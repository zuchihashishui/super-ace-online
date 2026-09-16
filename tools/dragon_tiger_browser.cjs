/* Real-browser tests against the disposable server in simple_auth_check.py.
   ACE_PLAYWRIGHT and ACE_CHROMIUM can point to installed browser tooling. */
const {chromium}=require(process.env.ACE_PLAYWRIGHT||'playwright');
const assert=require('node:assert/strict'),fs=require('node:fs');
(async()=>{
 const browser=await chromium.launch({executablePath:process.env.ACE_CHROMIUM||undefined,headless:true,args:['--no-sandbox','--disable-dev-shm-usage'],env:{...process.env}});
 try{
 const context=await browser.newContext({viewport:{width:390,height:844},reducedMotion:'reduce'});
 const page=await context.newPage(),errors=[];page.on('pageerror',e=>errors.push(e.message));
 await page.goto(process.env.ACE_TEST_URL||'http://127.0.0.1:18085');
 await page.waitForFunction(()=>document.getElementById('connection').textContent==='Guest');
 await page.locator('#menuToggle').click();await page.locator('[data-view=dragonTigerView]').click();
 await page.locator('#dragonTigerView').waitFor({state:'visible'});
 assert.equal(await page.locator('#dtBet').isDisabled(),true);
 await page.locator('#dtDeal').click();assert.equal(await page.locator('#loginDialog').evaluate(e=>e.open),true);
 await page.locator('#previewTheme').click();
 const username='dtui'+Date.now();
 await page.evaluate(async username=>{await api('register','POST',{username,password:'123456',rePassword:'123456'});await connect();await showView('dragonTigerView')},username);
 await page.waitForFunction(()=>ready&&!polling&&!dtBusy);
 assert.equal(await page.locator('#dtBet').inputValue(),'5');
 for(const width of [320,390,760,1440]){
  await page.setViewportSize({width,height:width>760?1000:900});
  assert(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth+1),'horizontal overflow '+width);
  for(const selector of ['#dtBet','#dtDeal','[data-dt-side=DRAGON]','[data-dt-side=TIGER]']){
   const rect=await page.locator(selector).boundingBox();assert(rect.width>=40&&rect.height>=40,'touch target '+selector+' '+width);
  }
  if(process.env.ACE_SCREENSHOTS){fs.mkdirSync(process.env.ACE_SCREENSHOTS,{recursive:true});await page.screenshot({path:process.env.ACE_SCREENSHOTS+'/dragon-tiger-'+width+'.png',fullPage:true})}
 }
 await page.setViewportSize({width:390,height:844});
 await page.emulateMedia({reducedMotion:'no-preference'});
 await page.locator('[data-dt-side=TIGER]').click();await page.locator('#dtBet').fill('7.55');
 await page.locator('#dtDeal').click();
 await page.waitForFunction(()=>dtLast&&!dtBusy&&!polling);
 const first=await page.evaluate(()=>dtLast);assert.equal(first.side,'TIGER');assert.equal(first.betCents,755);assert.equal(await page.locator('.dt-card.revealed').count(),4);
 assert.equal(first.balanceCents,first.balanceBeforeCents-first.betCents+first.payoutCents);
 assert(await page.locator('#dtBalance').evaluate(el=>el.getBoundingClientRect().height<parseFloat(getComputedStyle(el).fontSize)*1.6),'balance must stay on one line');
 await page.waitForTimeout(600);
 if(process.env.ACE_SCREENSHOTS)await page.screenshot({path:process.env.ACE_SCREENSHOTS+'/dragon-tiger-result.png',fullPage:true});
 // Drop the response after the server commits, then reload and recover the exact UUID.
 await page.route('**/api/dragon-tiger/rounds?*',async route=>{
  if(route.request().method()==='POST'){await route.fetch();await route.abort('failed');}
  else await route.continue();
 });
 await page.locator('#dtDeal').click();
 await page.waitForFunction(()=>dtPending&&!dtBusy&&!polling);
 const original=await page.evaluate(()=>dtPending.body.requestId);
 await page.unroute('**/api/dragon-tiger/rounds?*');
 await page.reload();await page.waitForFunction(()=>ready&&!polling);
 await page.evaluate(()=>showView('dragonTigerView'));
 await page.waitForFunction(()=>dtPending&&!polling);
 assert.equal(await page.locator('#dtLobby').isDisabled(),true);
 await page.locator('#dtDeal').click();await page.waitForFunction(()=>!dtPending&&!dtBusy&&dtLast&&!polling);
 assert.equal(await page.evaluate(()=>dtLast.requestId),original);
 assert.equal(await page.evaluate(()=>dtRows.filter(r=>r.requestId===dtLast.requestId).length),1);
 assert.equal(await page.evaluate(()=>dtRows.length),2);
 // Club uses a separate empty wallet: no accepted round and no negative balance.
 await page.locator('#dtClub').click();await page.waitForFunction(()=>gameMode==='CLUB'&&!polling&&!modeChanging);
 await page.locator('#dtDeal').click();await page.waitForFunction(()=>!dtBusy&&!polling);
 assert.equal(await page.evaluate(()=>balance),0);assert.equal(await page.evaluate(()=>dtRows.length),0);assert.equal(await page.evaluate(()=>dtPending),null);
 await page.evaluate(()=>setLanguage('fil'));assert.equal(await page.locator('[data-dt-side=TIE] b').textContent(),'TABLA');
 await page.locator('#dtRulesButton').click();assert((await page.locator('#dtRules').textContent()).includes('1.95'));await page.locator('#dtRulesClose').click();
 await page.evaluate(()=>setLanguage('en'));
 await page.locator('#dtLobby').click();await page.waitForFunction(()=>gameMode==='LOBBY'&&!polling&&!modeChanging);
 await page.locator('#dtBack').click();assert.equal(await page.evaluate(()=>activeView),'gameView');assert(await page.locator('#spin').isVisible());
 assert.deepEqual(errors,[]);
 console.log('PASS Chromium: guest preview, registration, four responsive widths, payouts, response loss + reload recovery, one charge, mode isolation, Filipino, rules and return to Super Ace.');
 }finally{await browser.close()}
})().catch(e=>{console.error(e);process.exitCode=1});
