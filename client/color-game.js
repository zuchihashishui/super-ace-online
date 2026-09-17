'use strict';
Object.assign(english,{cgRules:'Three independent dice, six equally likely colors. Select a chip and tap one or more colors during the 10-second betting window. Each tap adds a bet. Accepted bets cannot be cancelled. If your color appears once / twice / three times, the total return is 2× / 3× / 4× the stake, including the stake. No match returns zero. There is no progressive jackpot. Super Ace RTP settings do not apply.',cgHistoryNote:'Last 20 completed games. Percentages describe past dice, not the next result.'});
Object.assign(filipino,{'Choose a chip, then tap a color':'Pumili ng chip, saka pindutin ang kulay','Choose your color':'Piliin ang iyong kulay','Color Game':'Color Game','YELLOW':'DILAW','WHITE':'PUTI','PINK':'ROSAS','BLUE':'ASUL','RED':'PULA','GREEN':'BERDE','Three dice. Six colors.':'Tatlong dice. Anim na kulay.','Daily returns':'Mga balik ngayong araw','No payouts yet today.':'Wala pang balik ngayong araw.','Total returned · includes stakes':'Kabuuang ibinalik · kasama ang taya','Three matches':'Tatlong tugma','Gross return':'Kabuuang balik','Rolling…':'Gumugulong…','Waiting for next round':'Naghihintay ng susunod na laro','1 match':'1 tugma','2 matches':'2 tugma','3 matches':'3 tugma','Color history':'Kasaysayan ng kulay','History not available yet.':'Hindi pa makuha ang kasaysayan.',cgRules:'Tatlong hiwalay na dice na may anim na kulay at pantay na pagkakataon. Pumili ng chip at tumaya sa isa o maraming kulay sa loob ng 10 segundo. Bawat pindot ay dagdag na taya. Hindi maaaring kanselahin ang tinanggap na taya. Kapag lumabas ang kulay nang isa / dalawa / tatlong beses, ang kabuuang balik ay 2× / 3× / 4× kasama ang puhunan. Walang tugma: walang balik. Walang progressive jackpot. Hindi naaangkop ang RTP ng Super Ace.',cgHistoryNote:'Huling 20 natapos na laro. Ang porsiyento ay mula sa nakaraang dice, hindi hula sa susunod.'});
const cgColors=['YELLOW','WHITE','PINK','BLUE','RED','GREEN'],cgSymbols=['☀','◆','♥','●','★','♣'];
let cgState=null,cgOffset=0,cgOwner='',cgBets=[],cgQueue=[],cgChip=5,cgSending=false,cgFetching=false,cgBusy=false,cgRows=[],cgHistoryRows=[],cgCrowd=null,cgShown=null,cgLastFetch=0,cgError='',cgTick=-1,cgHistoryAt=0,cgStamp='',cgGeneration=0,cgMutation=0,cgHistoryVersion=0,cgNetworkError='',cgRevealTimer=null;
function cgKey(){return (player?.id||'guest')+':'+gameMode}
function cgNow(){return Date.now()+cgOffset}
function cgBetting(){return cgState&&Date.now()-cgLastFetch<4500&&cgNow()<cgState.bettingClosesAt}
function cgLocked(){return cgSending||cgBusy||cgQueue.length>0||cgBets.some(b=>!b.outcome)}
function cgSave(){try{if(player&&cgOwner===cgKey())sessionStorage.setItem('cg-queue-'+cgOwner,JSON.stringify(cgQueue))}catch{}}
function cgReset(){if(cgOwner===cgKey())return;cgOwner=cgKey();cgGeneration++;cgMutation++;cgHistoryVersion++;clearTimeout(cgRevealTimer);cgRevealTimer=null;cgBusy=false;$('cgCase').classList.remove('rolling');cgLastFetch=0;cgNetworkError='';cgBets=[];cgQueue=[];cgRows=[];cgHistoryRows=[];cgCrowd=null;cgShown=null;cgState=null;cgError='';cgStamp='';cgHistoryAt=0;try{if(player)cgQueue=JSON.parse(sessionStorage.getItem('cg-queue-'+cgOwner)||'[]').filter(q=>q.playerId===player.id&&q.mode===gameMode)}catch{}cgDice();}
function cgAmount(side){return cgBets.filter(b=>b.tableRoundId===cgState?.roundId&&(!side||b.side===side)).reduce((n,b)=>n+b.betCents,0)}
function cgQueued(side){return cgQueue.filter(q=>!side||q.body.side===side).reduce((n,q)=>n+q.body.betCents,0)}
function cgCue(kind){if(activeView!=='colorGameView'||document.hidden||!sound||!volume)return;openAudio();if(kind==='win'){[523,659,784,1047].forEach((n,i)=>tone(n,.15,'triangle',i*.08))}else if(kind==='roll'){[240,320,260,400].forEach((n,i)=>tone(n,.05,'triangle',i*.07))}else tone(kind==='tick'?900:kind==='chip'?660:510,.05,'sine')}
function cgDice(outcome){$('cgDice').replaceChildren(...[0,1,2].map((i)=>{const face=outcome?.dice[i],box=document.createElement('div');box.className='cg-die '+(face||'hidden-die');box.setAttribute('aria-label',face?t(face):'Hidden die');box.innerHTML='<span class="cg-face">'+(face?cgSymbols[cgColors.indexOf(face)]:'?')+'</span><small>'+(face?t(face):'')+'</small>';return box}));}
function cgUpdate(){
 if(!$('cgTable'))return;cgReset();const betting=cgBetting(),blocked=!betting||cgBusy||busy&&!cgSending||modeChanging||Boolean(pending)||autoActive()||free>0||Boolean(player&&!ready);
 $('cgBalance').textContent=player?fmt(balance):'—';$('cgUnit').textContent=gameMode==='LOBBY'?'Gold':'chips';$('cgTotal').textContent=money(cgAmount());$('cgPending').textContent=cgQueued()?t('Pending')+' +'+money(cgQueued()):'';
 $('cgStatus').textContent=cgNetworkError||cgError||(!player?t('Sign in to place a bet'):free>0?t('Play Super Ace to finish your free spins first.'):cgSending?t('Confirming your bet…'):t(betting?'Choose a chip, then tap a color':'Waiting for next round'));
 $('cgRetry').hidden=!cgQueue.length||cgSending||!cgError;
 for(const b of document.querySelectorAll('[data-cg-chip]')){b.setAttribute('aria-pressed',String(+b.dataset.cgChip===cgChip));b.disabled=cgBusy;}
 for(const [id,mode]of[['cgLobby','LOBBY'],['cgClub','CLUB']]){$(id).setAttribute('aria-pressed',String(gameMode===mode));$(id).disabled=cgLocked()||busy||modeChanging;}
 const signature=cgOwner+':'+cgState?.roundId+':'+language+':'+cgBets.map(b=>b.requestId).join(',');
 for(const b of document.querySelectorAll('[data-cg-side]')){
  const side=b.dataset.cgSide;b.disabled=blocked;b.classList.toggle('placed',cgAmount(side)>0);b.querySelector('.cg-own').textContent=t('Your bet')+' '+money(cgAmount(side));b.querySelector('.cg-pending').textContent=cgQueued(side)?'+'+money(cgQueued(side))+' '+t('Pending'):'';
  const pool=cgCrowd?.sides.find(s=>s.side===side);b.querySelector('.cg-others').textContent=t('Others')+' '+(pool?money(pool.othersCents):'—');
  if(signature!==cgStamp){const groups=new Map();cgBets.filter(r=>r.tableRoundId===cgState?.roundId&&r.side===side).forEach(r=>groups.set(r.betCents,(groups.get(r.betCents)||0)+1));b.querySelector('.cg-stacks').innerHTML=[...groups].map(([c,n])=>'<span class="cg-stack" data-cents="'+c+'" data-count="'+n+'" aria-label="'+n+' × '+c/100+'">'+Array.from({length:Math.min(n,4)},(_,i)=>'<i style="--layer:'+i+'">'+c/100+'</i>').join('')+'<small>×'+n+'</small></span>').join('');}
 }
 cgStamp=signature;$('cgBettors').textContent=t('Bettors this round')+': '+(cgCrowd?.bettors??'—');$('cgSound').textContent=sound&&volume?'♫':'♫ ×';
}
function cgAuthFailure(error){if(error.status===401){ready=false;if(!document.querySelector('dialog[open]'))showLogin();}}
async function cgFetch(){
 if(cgFetching||cgSending||cgBusy||activeView!=='colorGameView')return;cgReset();cgFetching=true;const owner=cgOwner,mode=gameMode,mutation=cgMutation,generation=cgGeneration;
 const current=()=>owner===cgKey()&&generation===cgGeneration&&mutation===cgMutation&&!cgSending;
 try{
  const state=await api('color-game/table?mode='+mode),receivedAt=Date.now();if(!current())return;
  const [bets,crowd]=await Promise.all([player?api('color-game/bets?mode='+mode+'&roundId='+state.roundId):[],api('color-game/crowd?mode='+mode+'&roundId='+state.roundId)]);if(!current())return;
  const wallet=player?await api('me?mode='+mode):null;if(!current())return;
  cgOffset=state.serverTime-receivedAt;
  if(cgState?.roundId!==state.roundId){cgShown=null;cgDice();$('cgResult').textContent=t('Choose your color');document.querySelectorAll('[data-cg-side]').forEach(b=>b.classList.remove('winning'));}
  cgState=state;cgBets=bets;cgCrowd=crowd;if(wallet){applyWallet(wallet);ready=canPlay(wallet);}cgLastFetch=Date.now();cgNetworkError='';
  if(state.outcome&&cgShown!==state.roundId){cgShown=state.roundId;cgReveal(state.outcome);void cgHistory();}else if(Date.now()-cgHistoryAt>15000)void cgHistory();
 }catch(e){if(owner===cgKey()){cgNetworkError=t('Reconnecting…')+' '+errorText(e);cgCrowd=null;cgAuthFailure(e);}}
 finally{cgFetching=false;cgUpdate();if(cgQueue.length&&!cgSending&&ready)void cgDrain();}
}
function cgResultText(outcome){const payout=cgBets.reduce((n,b)=>n+b.payoutCents,0),bet=cgAmount(),net=payout-bet;$('cgResult').textContent=outcome.dice.map(c=>t(c)).join(' · ')+(bet?' | '+t('Net')+' '+(net>0?'+':'')+money(net):'');return net;}
function cgReveal(outcome){
 const owner=cgOwner,generation=cgGeneration,round=cgState?.roundId;cgBusy=true;clearTimeout(cgRevealTimer);$('cgCase').classList.add('rolling');$('cgResult').textContent=t('Rolling…');cgCue('roll');
 cgRevealTimer=setTimeout(()=>{cgRevealTimer=null;if(owner!==cgKey()||generation!==cgGeneration||cgState?.roundId!==round){cgBusy=false;$('cgCase').classList.remove('rolling');return;}cgDice(outcome);$('cgCase').classList.remove('rolling');document.querySelectorAll('[data-cg-side]').forEach(b=>b.classList.toggle('winning',outcome.dice.includes(b.dataset.cgSide)));const net=cgResultText(outcome);if(net>0)cgCue('win');cgBusy=false;cgUpdate();},document.hidden||matchMedia('(prefers-reduced-motion: reduce)').matches?0:850);
}
function cgPlay(side){
 if(!player){showLogin();return}if(!cgBetting()||cgBusy||modeChanging||pending||autoActive()||free>0||!ready)return;if(cgQueue.length>=50)return;
 if(Math.round(balance*100)-cgQueued()<cgChip*100){cgError=errorText({code:'INSUFFICIENT_FUNDS'});cgUpdate();return;}
 openAudio();cgError='';cgMutation++;cgQueue.push({playerId:player.id,mode:gameMode,body:{requestId:requestUuid(),tableRoundId:cgState.roundId,side,betCents:cgChip*100}});cgSave();cgUpdate();void cgDrain();
}
async function cgDrain(){
 if(cgSending||cgBusy||!cgQueue.length||!ready)return;cgSending=true;busy=true;const owner=cgOwner;
 try{while(cgQueue.length&&owner===cgKey()){
  const q=cgQueue[0];if(q.body.expectedRevision===undefined){const w=await api('me?mode='+q.mode);if(owner!==cgKey())break;applyWallet(w);q.body.expectedRevision=revision;cgSave();}
  try{const r=await api('color-game/rounds?mode='+q.mode,'POST',q.body);if(owner!==cgKey())break;cgQueue.shift();cgMutation++;cgSave();if(r.tableRoundId===cgState?.roundId&&!cgBets.some(b=>b.requestId===r.requestId))cgBets.push(r);cgCue('chip');const w=await api('me?mode='+q.mode);if(owner!==cgKey())break;applyWallet(w);cgUpdate();}
  catch(e){if(e.code==='STALE_STATE'){const w=await api('me?mode='+q.mode);if(owner!==cgKey())break;applyWallet(w);q.body.expectedRevision=revision;cgSave();continue;}if(e.status===401){cgError=errorText(e);cgAuthFailure(e);break;}if(e.status>=400&&e.status<500){cgQueue.shift();cgMutation++;cgSave();cgError=errorText(e);continue;}cgError=t('Connection lost. Retry uses the same request and cannot charge twice.');break;}
 }}catch(e){if(owner===cgKey()){cgError=errorText(e);cgAuthFailure(e);}}finally{cgSending=false;busy=false;cgSave();cgUpdate();}
}
async function cgHistory(){
 const owner=cgKey(),mode=gameMode,version=++cgHistoryVersion;cgHistoryAt=Date.now();
 try{const [rows,bets,winners]=await Promise.all([api('color-game/table/history?mode='+mode),player?api('color-game/rounds?mode='+mode):[],api('color-game/winners?mode='+mode)]);if(owner!==cgKey()||version!==cgHistoryVersion)return;cgHistoryRows=rows;cgRows=bets;cgHistoryRender();$('cgWinners').innerHTML=winners.length?winners.map((r,i)=>'<li><b>'+String(i+1).padStart(2,'0')+'</b><span>'+esc(r.displayName)+'</span><strong>'+money(r.payoutCents)+'</strong></li>').join(''):'<li class="cg-empty">'+t('No payouts yet today.')+'</li>';}catch{if(owner===cgKey()&&version===cgHistoryVersion){$('cgHistoryList').textContent=t('History not available yet.');$('cgWinners').textContent=t('History not available yet.');}}
}
function cgHistoryRender(){
 const dice=cgHistoryRows.flatMap(r=>r.outcome.dice);$('cgFrequency').innerHTML=cgColors.map((c,i)=>'<div><span class="cg-swatch '+c+'">'+cgSymbols[i]+'</span><b>'+t(c)+'</b><small>'+(dice.length?(dice.filter(v=>v===c).length*100/dice.length).toFixed(1):'0')+'%</small></div>').join('');
 $('cgHistoryList').innerHTML=cgHistoryRows.length?cgHistoryRows.map(r=>{const bets=cgRows.filter(b=>b.tableRoundId===r.tableRoundId),stake=bets.reduce((n,b)=>n+b.betCents,0),payout=bets.reduce((n,b)=>n+b.payoutCents,0);return '<div class="cg-history-row"><span>#'+r.tableRoundId+'<small>'+esc(stamp(r.createdAt))+'</small></span><span class="cg-mini-dice">'+r.outcome.dice.map(c=>'<i class="cg-swatch '+c+'" title="'+t(c)+'" aria-label="'+t(c)+'">'+cgSymbols[cgColors.indexOf(c)]+'</i>').join('')+'</span><span>'+t('Bet')+' '+money(stake)+'<small>'+t('Net')+' '+(bets.length?money(payout-stake):'—')+'</small></span></div>';}).join(''):'<p>'+t('No completed games yet.')+'</p>';
 $('cgRecent').innerHTML=cgHistoryRows.slice(0,5).map(r=>'<span>'+r.outcome.dice.map(c=>'<i class="cg-swatch '+c+'" title="'+t(c)+'">'+cgSymbols[cgColors.indexOf(c)]+'</i>').join('')+'</span>').join('');
}
async function cgLoad(){cgReset();cgUpdate();await cgFetch();await cgHistory();}
function cgClock(){if(activeView!=='colorGameView')return;cgUpdate();if(cgState){const betting=cgBetting(),sec=Math.max(0,Math.ceil(((betting?cgState.bettingClosesAt:cgState.nextRoundAt)-cgNow())/1000));$('cgCountdown').textContent=String(sec).padStart(2,'0');$('cgPhase').textContent=t(betting?'BETTING OPEN':'CARDS OPEN');$('cgClock').classList.toggle('urgent',betting&&sec<=3);if(cgTick!==sec){cgTick=sec;if(betting&&sec<=3)cgCue('tick');}}
 if(Date.now()-cgLastFetch>900&&!cgSending&&!cgBusy)void cgFetch();}
document.querySelectorAll('[data-cg-side]').forEach(b=>b.onclick=()=>cgPlay(b.dataset.cgSide));document.querySelectorAll('[data-cg-chip]').forEach(b=>b.onclick=()=>{cgChip=+b.dataset.cgChip;cgCue('select');cgUpdate();});
$('cgLobby').onclick=()=>switchMode('LOBBY');$('cgClub').onclick=()=>switchMode('CLUB');$('cgRetry').onclick=()=>cgDrain();$('cgRulesButton').onclick=()=>$('cgRules').showModal();$('cgRulesClose').onclick=()=>$('cgRules').close();$('cgHistoryButton').onclick=()=>{$('cgHistoryDialog').showModal();void cgHistory();};$('cgHistoryClose').onclick=()=>$('cgHistoryDialog').close();$('cgHistoryRefresh').onclick=()=>cgHistory();$('cgSound').onclick=()=>{$('sound').click();cgUpdate();if(sound)cgCue('win');};
const cgOldHelp=$('help').onclick;$('help').onclick=()=>activeView==='colorGameView'?$('cgRules').showModal():cgOldHelp();
const cgOldLanguage=setLanguage;setLanguage=value=>{cgOldLanguage(value);cgHistoryRender();cgUpdate();if(cgState?.outcome&&cgShown===cgState.roundId&&!cgBusy){cgDice(cgState.outcome);cgResultText(cgState.outcome);}};
document.addEventListener('visibilitychange',()=>{if(!document.hidden&&activeView==='colorGameView')void cgLoad();});setInterval(cgClock,200);$('cgResult').removeAttribute('data-i18n');cgDice();localize();
