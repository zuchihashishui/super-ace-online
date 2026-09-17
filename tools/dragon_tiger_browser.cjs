/* Integration against the actual packaged server and a disposable database. */
const {chromium}=require(process.env.ACE_PLAYWRIGHT||'playwright');
const assert=require('node:assert/strict'),fs=require('node:fs');
(async()=>{
 const browser=await chromium.launch({executablePath:process.env.ACE_CHROMIUM||undefined,headless:true,args:['--no-sandbox','--disable-dev-shm-usage'],env:{...process.env}});
 try{
  const context=await browser.newContext({viewport:{width:390,height:844},reducedMotion:process.env.ACE_MOTION||'reduce'}),page=await context.newPage(),errors=[];
  page.on('pageerror',e=>errors.push(e.message));await page.goto(process.env.ACE_TEST_URL||'http://127.0.0.1:18085');await page.waitForFunction(()=>document.getElementById('connection').textContent==='Guest');
  await page.evaluate(()=>showView('dragonTigerView'));await page.waitForFunction(()=>dtBetting()&&!dtBusy,null,{timeout:20000});
  await page.locator('[data-dt-side=DRAGON]').click();assert.equal(await page.locator('#loginDialog').evaluate(e=>e.open),true);await page.locator('#previewTheme').click();
  await page.evaluate(async username=>{await api('register','POST',{username,password:'123456',rePassword:'123456'});await connect();await showView('dragonTigerView')},'chips'+Date.now());
  await page.waitForFunction(()=>ready&&!polling&&dtTableState);assert.equal(await page.locator('[data-dt-chip][aria-pressed=true]').textContent(),'5');
  await page.locator('#dtAudioButton').click();await page.locator('#dtAudioTest').click();await page.waitForFunction(()=>audioCtx?.state==='running');assert(await page.evaluate(()=>sound&&volume>0));
  assert(await page.evaluate(async()=>{const analyser=audioCtx.createAnalyser();analyser.fftSize=2048;masterGain.connect(analyser);dtCue('win');let peak=0;for(let i=0;i<8;i++){await new Promise(r=>setTimeout(r,35));const values=new Float32Array(analyser.fftSize);analyser.getFloatTimeDomainData(values);for(const v of values)peak=Math.max(peak,Math.abs(v))}masterGain.disconnect(analyser);analyser.disconnect();return peak>0.001}),'sound must produce a non-zero audio signal');
  await page.locator('#dtVolume').evaluate(e=>{e.value='35';e.dispatchEvent(new Event('input',{bubbles:true}))});assert.equal(await page.evaluate(()=>volume),.35);await page.locator('#dtAudioToggle').click();assert.equal(await page.evaluate(()=>sound),false);await page.waitForFunction(()=>masterGain.gain.value===0);await page.locator('#dtAudioToggle').click();await page.locator('#dtAudioClose').click();
  for(const width of [320,390,760,844,1440]){
   await page.setViewportSize({width,height:width===844?390:width>760?1000:900});assert(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth+1),'overflow '+width);
   const chipPanel=await page.locator('.dt-chip-console').boundingBox(),betPanel=await page.locator('.dt-bets').boundingBox();assert(betPanel.y+betPanel.height<=chipPanel.y+1,'chip controls must not obscure betting totals '+width);
   for(const selector of ['[data-dt-chip="5"]','[data-dt-chip="20"]','[data-dt-side=DRAGON]']){const r=await page.locator(selector).boundingBox();assert(r.width>=44&&r.height>=44,'touch target '+selector)}
   if(process.env.ACE_SCREENSHOTS){fs.mkdirSync(process.env.ACE_SCREENSHOTS,{recursive:true});await page.screenshot({path:process.env.ACE_SCREENSHOTS+'/chips-'+width+'.png',fullPage:true})}
  }
  await page.setViewportSize({width:390,height:844});await page.waitForFunction(()=>dtBetting()&&dtTableState.bettingClosesAt-dtServerNow()>6500&&!dtBusy&&!dtSending,null,{timeout:20000});
  const round=await page.evaluate(()=>dtTableState.roundId),before=await page.evaluate(()=>balance);
  await page.locator('[data-dt-chip="10"]').click();await page.locator('[data-dt-side=DRAGON]').click({clickCount:3,delay:70});
  await page.locator('[data-dt-chip="5"]').click();await page.locator('[data-dt-side=TIE]').click();
  await page.locator('[data-dt-chip="20"]').click();await page.locator('[data-dt-side=TIGER]').click();
  await page.waitForFunction(()=>dtBets.length===5&&!dtSending&&dtQueue.length===0);
  assert.equal(await page.locator('[data-dt-side=DRAGON] .dt-chip-stack[data-cents="1000"]').getAttribute('data-count'),'3');
  assert.equal(await page.locator('[data-dt-side=DRAGON] .dt-stacked-chip').count(),3);assert.match(await page.locator('#dtOwnChipCount').textContent(),/^5 /);
  assert.equal(await page.evaluate(()=>dtTotals('DRAGON')),3000);assert.equal(await page.evaluate(()=>dtTotals('TIE')),500);assert.equal(await page.evaluate(()=>dtTotals('TIGER')),2000);
  assert.equal(await page.evaluate(()=>balance),before-55);assert.equal(await page.locator('.dt-card.revealed').count(),0);assert(await page.evaluate(()=>dtBets.every(b=>!b.outcome&&b.payoutCents===0)));
  assert.match(await page.locator('[data-dt-side=DRAGON] .dt-side-total').textContent(),/30/);
  // Reload must restore all accepted chips from the server, not just the last click.
  await page.reload();await page.waitForFunction(()=>ready&&!polling);await page.evaluate(()=>showView('dragonTigerView'));await page.waitForFunction(()=>dtBets.length===5);
  await page.waitForFunction(id=>dtLast?.tableRoundId===id&&!dtBusy,round,{timeout:20000});const first=await page.evaluate(()=>dtLast);
  assert.equal(first.betCents,5500);assert.equal(await page.locator('.dt-card.revealed').count(),2);assert.equal(await page.evaluate(()=>balance),before-55+first.payoutCents/100);assert(await page.locator('[data-dt-side=DRAGON]').isDisabled());
  assert.equal(await page.locator('#dtDeal').count(),0);
  const historyRow=page.locator('.dt-history-row[data-round-id="'+round+'"]');assert.equal(await historyRow.count(),1);assert.match(await historyRow.textContent(),/Bet 55/);assert.match(await historyRow.textContent(),/DRAGON 30/);
  assert.equal(await page.evaluate(()=>volume),.35);assert.equal(await page.evaluate(()=>sound),true);
  await page.locator('#dtRoad .dt-bead').last().click();assert.equal(await page.locator('#dtInspect').evaluate(e=>e.open),true);assert.equal(await page.locator('#dtInspect .dt-card.revealed').count(),2);await page.locator('#dtInspectClose').click();assert.equal(await page.locator('#dtHistorySummary>span').count(),3);
  await page.evaluate(()=>{const saved=dtTableRows;dtTableRows=Array.from({length:25},(_,i)=>({...saved[0],tableRoundId:900000-i}));dtHistoryRender();if(document.querySelectorAll('.dt-history-row').length!==20||document.querySelectorAll('.dt-bead').length!==20)throw Error('History must display exactly 20 games');dtTableRows=saved;dtHistoryRender()});
  // A response can be lost after acceptance: the same UUID must never charge twice.
  await page.waitForFunction(id=>dtTableState.roundId!==id&&dtBetting()&&dtTableState.bettingClosesAt-dtServerNow()>6500&&!dtBusy,round,{timeout:20000});
  let lost=false;await page.route('**/api/dragon-tiger/rounds?*',async route=>{if(route.request().method()==='POST'&&!lost){lost=true;await route.fetch();await route.abort('failed')}else await route.continue()});
  await page.locator('[data-dt-chip="15"]').click();await page.locator('[data-dt-side=TIGER]').click();await page.waitForFunction(()=>dtQueue.length===1&&!dtSending);
  const request=await page.evaluate(()=>dtQueue[0].body.requestId),retryRound=await page.evaluate(()=>dtTableState.roundId);await page.unroute('**/api/dragon-tiger/rounds?*');await page.reload();await page.waitForFunction(()=>ready&&!polling);await page.evaluate(()=>showView('dragonTigerView'));
  await page.waitForFunction(()=>!dtQueue.length&&!dtSending&&dtBets.length===1);assert.equal(await page.evaluate(()=>dtBets[0].requestId),request);
  await page.waitForFunction(id=>dtLast?.tableRoundId===id&&!dtBusy,retryRound,{timeout:20000});assert.equal(await page.evaluate(id=>dtRows.filter(r=>r.requestId===id).length,request),1);
  await page.locator('#dtClub').click();await page.waitForFunction(()=>gameMode==='CLUB'&&!modeChanging&&!dtFetching);assert.equal(await page.evaluate(()=>balance),0);assert.equal(await page.evaluate(()=>dtTotals()),0);
  await page.waitForFunction(()=>dtBetting()&&!dtBusy,null,{timeout:20000});await page.locator('[data-dt-side=DRAGON]').click();assert.equal(await page.evaluate(()=>dtQueue.length),0);assert.equal(await page.evaluate(()=>balance),0);
  await page.evaluate(()=>setLanguage('fil'));assert.equal(await page.locator('[data-dt-side=TIE] b').textContent(),'TABLA');await page.locator('#dtRulesButton').click();assert((await page.locator('#dtRules').textContent()).includes('1.95'));await page.locator('#dtRulesClose').click();
  await page.evaluate(()=>setLanguage('en'));await page.locator('#dtBack').click();assert.equal(await page.evaluate(()=>activeView),'gameView');assert.deepEqual(errors,[]);
  // A second authenticated browser session places real bets; the first sees only others' totals.
  await page.evaluate(async()=>{await switchMode('LOBBY');await showView('dragonTigerView')});
  const other=await browser.newContext(),base=process.env.ACE_TEST_URL||'http://127.0.0.1:18085';
  const registration=await other.request.post(base+'/api/register',{headers:{'X-Game-Client':'web'},data:{username:'other'+Date.now(),password:'123456',rePassword:'123456'}});assert.equal(registration.status(),200);const otherMe=await registration.json();
  await page.waitForFunction(()=>dtBetting()&&dtTableState.bettingClosesAt-dtServerNow()>7500&&!dtSending&&!dtBusy,null,{timeout:22000});
  const sharedRound=await page.evaluate(()=>dtTableState.roundId);
  await page.locator('[data-dt-chip="5"]').click();await page.locator('[data-dt-side=DRAGON]').click({clickCount:2,delay:60});await page.waitForFunction(()=>dtBets.length===2&&!dtSending);
  const req={requestId:require('node:crypto').randomUUID(),tableRoundId:sharedRound,side:'DRAGON',betCents:2000,expectedRevision:otherMe.revision};
  const headers={'X-Game-Client':'web','X-CSRF-Token':otherMe.csrf};const receipt=await other.request.post(base+'/api/dragon-tiger/rounds?mode=LOBBY',{headers,data:req});assert.equal(receipt.status(),200);
  await other.request.post(base+'/api/dragon-tiger/rounds?mode=LOBBY',{headers,data:req});
  await page.waitForFunction(()=>dtCrowdState?.sides.find(s=>s.side==='DRAGON')?.othersCents===2000);
  const mine=await page.evaluate(()=>dtCrowdState.sides.find(s=>s.side==='DRAGON'));assert.equal(mine.ownCents,1000);assert.equal(mine.ownChipCount,2);assert.equal(mine.othersChipCount,1);assert.equal(mine.otherBettors,1);
  assert.equal(await page.locator('[data-dt-side=DRAGON] .dt-chip-stack[data-cents="500"] .dt-stacked-chip').count(),2);assert.match(await page.locator('[data-dt-side=DRAGON] .dt-other-value').textContent(),/^20/);
  const otherPool=await (await other.request.get(base+'/api/dragon-tiger/crowd?mode=LOBBY&roundId='+sharedRound)).json();assert.equal(otherPool.sides.find(s=>s.side==='DRAGON').othersCents,1000);
  const clubPool=await (await other.request.get(base+'/api/dragon-tiger/crowd?mode=CLUB&roundId='+sharedRound)).json();assert(clubPool.sides.every(s=>s.ownCents===0&&s.othersCents===0));
  if(process.env.ACE_SCREENSHOTS){await page.locator('.dt-bets').scrollIntoViewIfNeeded();await page.screenshot({path:process.env.ACE_SCREENSHOTS+'/multiplayer-stacks.png',fullPage:true})}
  await other.close();assert.deepEqual(errors,[]);
  console.log('PASS Chromium: chip selection, rapid cumulative/multi-side bets, hidden outcomes, 10-second clock, settlement, reload/retry idempotency, wallet isolation, responsive touch targets, Filipino, audio signal/mute/persistence and card inspection.');
 }finally{await browser.close()}
})().catch(e=>{console.error(e);process.exitCode=1});
