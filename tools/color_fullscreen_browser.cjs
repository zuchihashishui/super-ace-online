const {chromium}=require(process.env.ACE_PLAYWRIGHT||'playwright'),assert=require('node:assert/strict');
(async()=>{const browser=await chromium.launch({executablePath:process.env.ACE_CHROMIUM,headless:true,args:['--no-sandbox']});try{
for(const mode of ['native','native-phone','denied','missing','delayed','preexisting']){
 const phone=mode!=='native'&&mode!=='preexisting';const context=await browser.newContext({viewport:phone?{width:390,height:844}:{width:1280,height:900},hasTouch:phone,isMobile:phone}),p=await context.newPage(),errors=[];p.on('pageerror',e=>errors.push(e.message));
 await p.route('**/*',async route=>{if(route.request().resourceType()==='document'){const response=await route.fetch();return route.fulfill({response,body:(await response.text()).replace(/<script\b[^>]*>[\s\S]*?<\/script>/gi,'')});}return route.continue()});await p.goto(process.env.ACE_TEST_URL);
 await p.evaluate(mode=>{
  window.$=id=>document.getElementById(id);window.filipino={};window.activeView='gameView';window.blockEntry=false;window.fullRequests=0;window.fullExits=0;window.activation=[];
  window.showView=async id=>{if(blockEntry)return;activeView=id;document.querySelectorAll('main').forEach(e=>e.hidden=e.id!==id);await new Promise(r=>setTimeout(r,60))};
  const enter=document.createElement('button');enter.id='testEnter';enter.textContent='Enter Color Game';enter.onclick=()=>showView('colorGameView');enter.style.cssText='position:fixed;left:2px;top:2px;z-index:100';document.body.append(enter);
  const nativeRequest=document.documentElement.requestFullscreen?.bind(document.documentElement),nativeExit=document.exitFullscreen.bind(document);
  document.exitFullscreen=()=>{fullExits++;return nativeExit()};
  document.documentElement.requestFullscreen=()=>{fullRequests++;activation.push(navigator.userActivation.isActive);return nativeRequest()};
  if(mode==='denied')document.documentElement.requestFullscreen=()=>{fullRequests++;return Promise.reject(new Error('Denied'))};
  if(mode==='missing')document.documentElement.requestFullscreen=undefined;
  if(mode==='delayed'||mode==='preexisting'){
   window.mockFull=mode==='preexisting'?document.documentElement:null;Object.defineProperty(document,'fullscreenElement',{get:()=>mockFull});
   document.exitFullscreen=async()=>{fullExits++;mockFull=null;document.dispatchEvent(new Event('fullscreenchange'))};
   document.documentElement.requestFullscreen=()=>{fullRequests++;return new Promise(resolve=>window.resolveFullscreen=()=>{mockFull=document.documentElement;document.dispatchEvent(new Event('fullscreenchange'));resolve()})};
  }
 },mode);await p.addScriptTag({url:process.env.ACE_TEST_URL+'/color-landscape.js'});
 // A blocked game navigation must never request fullscreen.
 await p.evaluate(()=>blockEntry=true);await p.locator('#testEnter').click();assert.equal(await p.evaluate(()=>fullRequests),0);await p.evaluate(()=>blockEntry=false);await p.locator('#testEnter').click();await p.waitForFunction(()=>document.body.classList.contains('cg-immersive'));
 if(mode.startsWith('native')){
  await p.waitForFunction(()=>document.fullscreenElement===document.documentElement);assert.deepEqual(await p.evaluate(()=>activation),[true]);assert.equal(await p.evaluate(()=>fullRequests),1);
  const box=await p.locator('#colorGameView').boundingBox(),size=await p.evaluate(()=>({w:innerWidth,h:innerHeight}));assert(Math.abs(box.width-size.w)<2&&Math.abs(box.height-size.h)<2);
  await p.evaluate(()=>document.exitFullscreen());await p.waitForFunction(()=>!document.fullscreenElement);await p.waitForTimeout(100);assert.equal(await p.evaluate(()=>fullRequests),1,'do not re-enter after user exits');await p.locator('#cgMenuToggle').click();await p.locator('#cgFullscreen').click();await p.waitForFunction(()=>!!document.fullscreenElement);assert.equal(await p.evaluate(()=>fullRequests),2);
 }else if(mode==='denied'||mode==='missing'){
  await p.waitForFunction(()=>document.body.classList.contains('cg-rotated'));assert.equal(await p.evaluate(()=>!!document.fullscreenElement),false);await p.locator('#cgMenuToggle').tap();await p.locator('#cgFullscreen').tap();assert.equal(await p.evaluate(()=>fullRequests),mode==='denied'?2:0);await p.evaluate(()=>cgScreenLayout());assert.equal(await p.evaluate(()=>fullRequests),mode==='denied'?2:0);
 }else if(mode==='preexisting'){assert.equal(await p.evaluate(()=>fullRequests),0);}
 
 
 assert(await p.locator('#cgMenuOverlay').isHidden());
 if(mode==='denied'){
  const code=await (await p.request.get(process.env.ACE_TEST_URL+'/color-game.js')).text();
  await p.addScriptTag({content:code.slice(code.indexOf('// Decorative flights only:'))});
  await p.evaluate(()=>{document.body.dataset.cgQuality='high';cgChipFlight(document.querySelector('[data-cg-chip]'),document.querySelector('[data-cg-side]'),5)});
  assert.equal(await p.locator('.cg-flying-chip').count(),1);
  await p.waitForTimeout(650);assert.equal(await p.locator('.cg-flying-chip').count(),0);
  await p.evaluate(()=>{document.body.dataset.cgQuality='battery';cgChipFlight(document.querySelector('[data-cg-chip]'),document.querySelector('[data-cg-side]'),5)});
  assert.equal(await p.locator('.cg-flying-chip').count(),0);
 }
 if(mode==='denied')await p.screenshot({path:'/tmp/ace-v39-board.png'});
 await p.locator('#cgMenuToggle').click();assert(await p.locator('#cgMenuPanel').isVisible());
 assert.equal(await p.locator('#cgTable').evaluate(e=>e.inert),true);if(mode==='denied')await p.screenshot({path:'/tmp/ace-v39-menu.png'});
 for(const id of ['cgExit','cgLobby','cgClub','cgSound','cgRulesButton','cgFullscreen'])assert(await p.locator('#'+id).isVisible());
 await p.keyboard.press('Escape');assert(await p.locator('#cgMenuOverlay').isHidden());
 assert.equal(await p.locator('#cgTable').evaluate(e=>e.inert),false);
 await p.locator('#cgMenuToggle').click();await p.locator('#cgSound').click();assert(await p.locator('#cgMenuOverlay').isHidden());
 if(phone){const fit=await p.evaluate(()=>({board:document.querySelector('.cg-fiesta').offsetHeight,game:document.getElementById('colorGameView').clientHeight}));assert(Math.abs(fit.game-fit.board-9)<2);}
 await p.locator('#cgMenuToggle').click();await p.locator('#cgExit').click();await p.waitForFunction(()=>activeView==='gameView'&&!document.body.classList.contains('cg-immersive'));
 if(mode==='delayed'){await p.evaluate(()=>resolveFullscreen());await p.waitForFunction(()=>!document.fullscreenElement);assert.equal(await p.evaluate(()=>fullExits),1,'late fullscreen must close after leaving');}
 else if(mode==='preexisting'){assert(await p.evaluate(()=>!!document.fullscreenElement));assert.equal(await p.evaluate(()=>fullExits),0,'preserve user-owned fullscreen');}
 else await p.waitForFunction(()=>!document.fullscreenElement);
 assert(!(await p.evaluate(()=>document.body.classList.contains('cg-wide'))));assert.deepEqual(errors,[]);console.log('PASS fullscreen',mode);await context.close();
}
}finally{await browser.close()}})().catch(e=>{console.error(e);process.exit(1)});
