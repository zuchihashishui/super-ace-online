/* Actual frontend scripts in a DOM emulator, connected to a live disposable server.
   Requires jsdom; this verifies behavior, not browser layout or audible playback. */
const vm=require('node:vm');
const fs=require('fs'),path=require('path'),assert=require('node:assert/strict');
const {JSDOM}=require(process.env.ACE_JSDOM||'jsdom');
const base=process.env.ACE_TEST_URL,seed=JSON.parse(fs.readFileSync(process.env.ACE_TEST_SEED,'utf8'));
const root=path.join(__dirname,'../client');
const errors=[];let cookie='',failSpin=false,realSpinCount=0;
const dom=new JSDOM(fs.readFileSync(path.join(root,'index.html'),'utf8'),{url:base,runScripts:'outside-only',pretendToBeVisual:true});
const w=dom.window;
w.addEventListener('error',e=>errors.push(e.error||e.message));
w.matchMedia=()=>({matches:true,addEventListener(){},removeEventListener(){}});
w.HTMLElement.prototype.scrollIntoView=function(){};
w.HTMLDialogElement.prototype.showModal=function(){this.open=true;this.setAttribute('open','')};
w.HTMLDialogElement.prototype.close=function(){this.open=false;this.removeAttribute('open')};
w.AbortController=AbortController;
w.fetch=async(url,options={})=>{const r=await fetch(new URL(url,base),{...options,headers:{...options.headers,...(cookie?{Cookie:cookie}:{})}});const c=r.headers.get('set-cookie');if(c)cookie=c.split(';')[0];if(url.endsWith('/spins')){realSpinCount++;if(failSpin){failSpin=false;await r.text();throw Error('Simulated lost response AFTER commit')}}return r};
for(const file of ['i18n.js','game.js','portal.js','hierarchy.js'])new vm.Script(fs.readFileSync(path.join(root,file),'utf8'),{filename:file}).runInContext(dom.getInternalVMContext());
const sleep=ms=>new Promise(r=>setTimeout(r,ms));
async function wait(fn,message,timeout=20000){const start=Date.now();while(!fn()){if(Date.now()-start>timeout)throw Error('Timeout: '+message);await sleep(30)}}
function value(s){return w.eval(s)}
(async()=>{
 await sleep(500);assert.equal(w.document.getElementById('loginDialog').open,false);assert.equal(w.document.getElementById('spin').disabled,true);w.document.getElementById('signInNav').click();await wait(()=>w.document.getElementById('loginDialog').open,'login');assert.equal(w.document.documentElement.lang,'en');
 w.document.getElementById('registerToggle').click();const registration=w.document.getElementById('registerForm');registration.elements.username.value='guest'+Date.now();registration.elements.displayName.value='Registered Guest';registration.elements.password.value=seed.password;registration.elements.agentCode.value=seed.agentCode;registration.dispatchEvent(new w.Event('submit',{bubbles:true,cancelable:true}));await wait(()=>value('ready')&&!w.document.getElementById('loginDialog').open,'registration complete');assert.match(value('player.publicCode'),/^[0-9]{6}$/);await value("api('logout','POST')");value('ready=false;player=null;showLogin()');w.document.getElementById('registerToggle').click();
 const form=w.document.getElementById('loginForm');form.elements.username.value=seed.player;form.elements.password.value=seed.password;form.dispatchEvent(new w.Event('submit',{bubbles:true,cancelable:true}));
 await wait(()=>value('ready')&&!w.document.getElementById('loginDialog').open,'login complete');
 // Five selectable themes and two full static language catalogs.
 for(const key of ['emerald','ruby','sapphire','neon','classic']){w.document.getElementById('theme-'+key).click();assert.equal(w.document.body.dataset.theme,key)}
 value("setLanguage('fil')");assert.equal(w.document.documentElement.lang,'fil');assert.equal(w.document.getElementById('spinLabel').textContent,value('free>0')?'LIBRE':'IKOT');
 value("setLanguage('en')");assert.equal(w.document.getElementById('spinLabel').textContent,value('free>0')?'FREE':'SPIN');
 const untranslated=value("[...document.querySelectorAll('[data-i18n]')].map(e=>e.dataset.i18n).filter(k=>!Object.hasOwn(filipino,k))");assert.deepEqual([...new Set(untranslated)],[]);
 // Ordinary real server spin and lost-response idempotency through actual client retry logic.
 value('clearTimeout(pollTimer)');await wait(()=>!value('polling')&&!value('busy'),'poll idle');await value('spin()');assert.equal(value('pending'),null);const before=value('revision');failSpin=true;await sleep(270);await value('spin()');assert.ok(value('pending'));await value('spin()');assert.equal(value('pending'),null);assert.equal(value('revision'),before+1);
 await value("showView('rankingView')");assert.ok(w.document.getElementById('rankingTable').querySelector('table'));
 await value("showView('reportsView')");assert.ok(w.document.getElementById('reportTable').querySelector('table'));
 await value("showView('chipsView')");assert.ok(w.document.getElementById('chipTable').querySelector('table'));
 await value("showView('gameView')");await value("syncAuto(false)");
 // Stop polling, start server job, then close DOM entirely. A second client checks server progress.
 value('clearTimeout(pollTimer)');await wait(()=>!value('polling')&&!value('busy'),'idle');
 const csrf=value('player.csrf'),rev=value('revision'),bet=value('free>0?player.lockedBetCents:bets[betIndex]*100');
 const api=async(p,data)=>{const r=await fetch(base+'/api/'+p,{method:data?'POST':'GET',headers:{Cookie:cookie,'Content-Type':'application/json','X-Game-Client':'web','X-CSRF-Token':csrf},...(data?{body:JSON.stringify(data)}:{})});assert.ok(r.ok,p+' '+r.status);return r.json()};
 const job=await api('auto/start',{runId:crypto.randomUUID(),count:10,betCents:bet,expectedRevision:rev,turbo:true});
 const clientCookie=cookie;w.close();await sleep(6500);const state=await api('auto');assert.ok(state.wallet.revision>rev);assert.ok(state.job.completed+state.job.freeCompleted>=1);await api('auto/stop',{runId:job.runId});
 assert.equal(errors.length,0,errors.map(String).join('\n'));
 fs.writeFileSync(process.env.ACE_TEST_COOKIE||'/tmp/ace-restart-cookie.json',JSON.stringify({cookie:clientCookie,csrf,revision:state.wallet.revision,balance:state.wallet.balanceCents}));
 console.log('PASS: DOM login, 5 themes, en/fil catalog, real spin, lost-response retry, ranking/report/history views, autoplay after client closes; no JS errors.');
})().catch(e=>{console.error(e);w.close();process.exit(1)});
