// Visual regression of real packaged CSS/notification code with controlled UI fixtures.
// Serve BOOT-INF/classes/static extracted from the release JAR at ACE_TEST_URL.
const assert=require('node:assert/strict'),fs=require('node:fs');
const {chromium}=require(process.env.ACE_PLAYWRIGHT||'playwright');
(async()=>{
 const portable=process.env.ACE_PORTABLE_CHROMIUM?(await import(process.env.ACE_PORTABLE_CHROMIUM)).default:null;
 const browser=await chromium.launch({executablePath:portable?await portable.executablePath():process.env.ACE_CHROMIUM,headless:true,args:portable?portable.args:['--no-sandbox']});
 try{for(const reducedMotion of ['no-preference','reduce']){
  const context=await browser.newContext({viewport:{width:390,height:844},hasTouch:true,isMobile:true,reducedMotion});const p=await context.newPage(),errors=[];p.on('pageerror',e=>errors.push(e.message));
  await p.route('**/*',async route=>{if(route.request().resourceType()==='document'){const response=await route.fetch();return route.fulfill({response,body:(await response.text()).replace(/<script\b[^>]*>[\s\S]*?<\/script>/gi,'')});}return route.continue();});
  await p.goto(process.env.ACE_TEST_URL);
  await p.evaluate(()=>{
   // Native fullscreen has a separate suite; keep this one resizable for rotation fixtures.
   document.documentElement.requestFullscreen=()=>Promise.reject(new Error('CSS orientation fixture'));
   window.$=id=>document.getElementById(id);window.filipino={};window.t=s=>s;window.money=n=>(n/100).toFixed(2);window.fmt=n=>n.toLocaleString('en-US');window.player={id:'fixture-player',role:'PLAYER'};window.gameMode='LOBBY';window.activeView='gameView';window.ready=true;window.sound=false;window.volume=0;window.canPlay=()=>true;window.applyRole=()=>{};window.cgBusy=false;window.testNotices=[];window.testReads=[];window.errorText=e=>e.message;window.cgCue=()=>{};
   window.api=async(path)=>{if(path==='notifications')return testNotices;testReads.push(path);testNotices=[];return {ok:true};};
   window.toast=message=>{$('toast').textContent=message;$('toast').hidden=false};
   window.showView=async id=>{activeView=id;document.querySelectorAll('main').forEach(v=>v.hidden=v.id!==id)};
  });
  for(const src of ['victory.js','receipts.js','color-landscape.js'])await p.addScriptTag({url:process.env.ACE_TEST_URL+'/'+src});
  await p.evaluate(()=>showView('colorGameView'));
  async function angle(selector){return p.locator(selector).evaluate(el=>{let matrix=new DOMMatrix();for(let n=el;n;n=n.parentElement){const t=getComputedStyle(n).transform;if(t!=='none')matrix=new DOMMatrix(t).multiply(matrix);}return Math.round(Math.atan2(matrix.b,matrix.a)*180/Math.PI);});}
  async function inside(selector){const b=await p.locator(selector).boundingBox(),v=p.viewportSize();assert(b&&b.x>=-2&&b.y>=-2&&b.x+b.width<=v.width+2&&b.y+b.height<=v.height+2,selector+' outside '+JSON.stringify({b,v}));}
  let key=0;
  for(const size of [{width:390,height:844},{width:844,height:390},{width:360,height:800},{width:667,height:375},{width:320,height:568}]){
   await p.setViewportSize(size);await p.waitForFunction(v=>{const r=$('colorGameView').getBoundingClientRect();return Math.abs(r.width-v.width)<2&&Math.abs(r.height-v.height)<2},size);const target=size.height>size.width?90:0;
   for(const returned of [2000,10000]){
    await p.evaluate(({key,returned})=>{celebrateWin('colorGameView',key,1000,returned);clearTimeout(victoryTimer)}, {key:++key,returned});await p.waitForTimeout(500);
    assert.equal(await angle('.av-award'),target,'award must follow the game, without double rotation');await inside('.av-award');assert.equal(await p.locator('.av-award strong').textContent(),returned===10000?'BIG WIN':'YOU WIN');
    if(process.env.ACE_SCREENSHOTS&&reducedMotion==='reduce'&&returned===10000&&size.width===390){fs.mkdirSync(process.env.ACE_SCREENSHOTS,{recursive:true});await p.screenshot({path:process.env.ACE_SCREENSHOTS+'/win-portrait.png'});}
    await p.evaluate(()=>hideVictory());
   }
   await p.evaluate(()=>toast('Gold received · '+('A longer notification to check wrapping. ').repeat(8)));assert.equal(await angle('#toast'),target);await inside('#toast');await p.evaluate(()=>$('toast').hidden=true);
   for(const kind of ['JACKPOT','DAILY','TRANSFER']){
    await p.evaluate(async kind=>{testNotices=[{id:kind,kind,currency:kind==='TRANSFER'?'CHIPS':'GOLD',amountCents:300001,tier:'MINI',roundId:123,senderName:'LUCKY SEVEN'}];await pollReceipts();},kind);
    assert(await p.locator('#chipReceipt').evaluate(e=>e.open));assert.equal(await angle('#chipReceipt'),target);await inside('#chipReceipt');await p.locator('#receiptRead').tap();assert(!(await p.locator('#chipReceipt').evaluate(e=>e.open)));
   }
   console.log('PASS',reducedMotion,size.width+'x'+size.height,'normal/big win, toast, Jackpot/Gold/chip receipt and acknowledgment');
  }
  // Rotate while a win is visible, then leave the game; no leaked orientation/effect.
  await p.evaluate(()=>{celebrateWin('colorGameView','resize',1000,5000);clearTimeout(victoryTimer)});await p.setViewportSize({width:844,height:390});await p.waitForFunction(()=>!document.body.classList.contains('cg-rotated'));assert.equal(await angle('.av-award'),0);await inside('.av-award');await p.evaluate(()=>showView('gameView'));assert.equal(await p.locator('.ace-victory').count(),0);assert(!(await p.evaluate(()=>document.body.classList.contains('cg-wide'))));
  await p.evaluate(()=>{celebrateWin('gameView','other-game',1000,2000);clearTimeout(victoryTimer)});assert.equal(await angle('.av-award'),0);await p.evaluate(()=>hideVictory());
  await p.setViewportSize({width:1440,height:900});await p.evaluate(()=>showView('colorGameView'));assert(!(await p.evaluate(()=>document.body.classList.contains('cg-wide'))));await p.evaluate(()=>celebrateWin('colorGameView','desktop',1000,2000));assert.equal(await angle('.av-award'),0);await inside('.av-award');await p.waitForFunction(()=>!document.querySelector('.ace-victory'),null,{timeout:4000});
  assert.deepEqual(errors,[]);await context.close();
 }
 console.log('PASS desktop, rotation during win, automatic cleanup, other-game isolation and zero JS errors.');
 }finally{await browser.close()}
})().catch(e=>{console.error(e);process.exit(1)});
