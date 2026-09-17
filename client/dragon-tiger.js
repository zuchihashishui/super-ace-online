'use strict';
// Safe fallback until the optional presentation layer finishes loading.
function dtCue(kind){if(document.hidden||activeView!=='dragonTigerView')return;if(kind==='win')winChime(1);else tone(kind==='tick'?880:kind==='chip'?760:440,.06,'sine')}
Object.assign(english,{
 dtRulesCards:'A fresh 52-card deck is used each round. Dragon and Tiger receive one card each, without replacement.',
 dtRulesScore:'A = 1, 2–10 = face value, J = 11, Q = 12, K = 13. Higher rank wins. Equal ranks are a Tie; suits do not break ties.',
 dtRulesPay:'Select a chip (5, 10, 15, 20, 30, 50 or 100), then tap Dragon, Tiger or Tie. Each tap adds another bet. You may bet on several sides before the 10-second timer closes. Accepted bets cannot be cancelled. Dragon / Tiger return 1.95× on a win; Tie returns 9×. A tie refunds Dragon / Tiger stakes. Returns include the stake and are rounded to 0.01 for each bet.',
 dtRulesSeparate:'One-card Dragon Tiger with the displayed payout rules. Fixed payouts; Super Ace RTP settings do not apply. Virtual chips only.',
 dtRulesExample:'Example: select 10 and tap Dragon three times to bet 30. Select 5 and tap Tie to add 5 on Tie. Total bet: 35.'
});
Object.assign(filipino,{
 'Choose a chip, then tap a side':'Pumili ng chip, saka pindutin ang panig','Total bet':'Kabuuang taya','Your bet':'Iyong taya','Pending':'Kinukumpirma','BETTING OPEN':'BUKAS ANG TAYA','CARDS OPEN':'BUKAS ANG BARAAHA','Betting closes in':'Magsasara ang taya sa','Next round in':'Susunod na laro sa','Tap again to add more':'Pindutin muli para magdagdag','Retry same round':'Ulitin ang parehong taya','No bet placed · Table result':'Walang taya · Resulta ng mesa','Dealing cards…':'Namimigay ng baraha…','Dragon wins':'Panalo ang Dragon','Tiger wins':'Panalo ang Tiger','Tie game':'Tabla ang laro','You won':'Nanalo ka','Stake returned':'Ibinalik ang taya','No win this round':'Hindi nanalo sa larong ito','Your result is saved.':'Naka-save ang resulta.','Sign in to place a bet':'Mag-sign in upang tumaya','History unavailable. Tap Refresh.':'Hindi makuha ang kasaysayan. Pindutin ang Refresh.','No rounds yet. Choose a side to begin.':'Wala pang laro. Pumili ng panig.','Play Super Ace to finish your free spins first.':'Tapusin muna ang libreng ikot sa Super Ace.','TIE':'TABLA','BET ON':'TUMAYA SA','Available balance':'Magagamit na balanse','Return ×1.95':'Balik ×1.95','Return ×9':'Balik ×9','Recent rounds':'Mga nakaraang laro','YOUR TABLE':'IYONG MESA','Two-card duel':'Laban ng dalawang baraha','Two cards. One winning side.':'Dalawang baraha. Isang panalong panig.','Last digit wins':'Huling digit ang puntos','Choose your side':'Piliin ang iyong panig','Higher card wins · A low, K high':'Mas mataas na puntos ang panalo · 0–9',
 'Returns include your stake. Dragon / Tiger bets are refunded on a tie.':'Kasama ang taya sa balik. Ibinabalik ang taya sa Dragon / Tiger kapag tabla.',
 'Your last 50 rounds in this mode. Past results do not predict the next round.':'Huling 50 taya sa mode na ito. Hindi hinuhulaan ng nakaraan ang susunod na resulta.',
 'Independent two-card variant · Virtual chips only':'Hiwalay na dalawang-barahang laro · Virtual chips lamang',
 dtRulesCards:'Bagong 52 baraha bawat laro. Tig-isang baraha ang Dragon at Tiger, walang ibinabalik sa deck.',
 dtRulesScore:'A = 1, 2–10 = halaga ng baraha, J = 11, Q = 12, K = 13. Mas mataas ang panalo. Magkaparehong ranggo ay tabla; hindi batayan ang suit.',
 dtRulesPay:'Pumili ng chip (5, 10, 15, 20, 30, 50 o 100) at pindutin ang Dragon, Tiger o Tabla. Bawat pindot ay dagdag na taya. Maaaring tumaya sa maraming panig sa loob ng 10 segundo. Hindi makakansela ang tinanggap na taya. Balik: Dragon / Tiger 1.95×; Tabla 9×. Kapag tabla, ibinabalik ang taya sa Dragon / Tiger. Kasama ang puhunan sa balik; niro-round sa 0.01 bawat taya.',
 dtRulesSeparate:'Dragon Tiger na tig-isang baraha ayon sa nakasaad na payout. Hindi naaangkop ang RTP ng Super Ace. Virtual chips lamang.',
 dtRulesExample:'Halimbawa: chip 10, tatlong pindot sa Dragon = 30. Chip 5, isang pindot sa Tabla = 5. Kabuuang taya: 35.'
});
let dtBusy=false,dtSending=false,dtPending=null,dtQueue=[],dtBets=[],dtChip=5,dtSide='DRAGON',dtOwner='',dtRows=[],dtLast=null,dtTableState=null,dtClockOffset=0,dtShownRound=null,dtFetching=false,dtError='',dtTickSecond=-1,dtLastSync=0;
const dtWaiters=new Set();
let dtTableRows=[],dtHistoryVersion=0;
Object.assign(filipino,{'Last 20 completed games. Bets on the same game are combined.':'Huling 20 natapos na laro. Pinagsasama ang mga taya sa parehong laro.','No completed games yet.':'Wala pang natapos na laro.','No bet':'Walang taya','Game':'Laro','History unavailable. Tap Refresh.':'Hindi makuha ang kasaysayan. Pindutin ang Refresh.'});
function dtKey(){return player?player.id+':'+gameMode:''}
function dtLocked(){return dtBusy||dtSending||dtQueue.length>0||dtBets.some(b=>!b.outcome)}
function dtSave(){dtPending=dtQueue[0]||null;try{if(dtOwner)sessionStorage.setItem('dt-queue-'+dtOwner,JSON.stringify(dtQueue))}catch{}}
function dtServerNow(){return Date.now()+dtClockOffset}
function dtBetting(){return dtTableState&&dtServerNow()<dtTableState.bettingClosesAt}
function dtReserved(){return dtQueue.reduce((n,q)=>n+q.body.betCents,0)}
function dtTotals(side){const id=dtTableState?.roundId;return dtBets.filter(b=>b.tableRoundId===id&&(!side||b.side===side)).reduce((n,b)=>n+b.betCents,0)}
function dtQueued(side){const id=dtTableState?.roundId;return dtQueue.filter(q=>q.body.tableRoundId===id&&(!side||q.body.side===side)).reduce((n,q)=>n+q.body.betCents,0)}
function dtUpdate(){
 if(!$('dtTotal'))return;
 const key=dtKey();if(key!==dtOwner&&!dtSending&&!dtBusy){dtOwner=key;dtBets=[];dtQueue=[];dtRows=[];dtLast=null;dtShownRound=null;dtError='';try{const saved=JSON.parse(sessionStorage.getItem('dt-queue-'+key)||'[]');if(key&&Array.isArray(saved))dtQueue=saved.filter(q=>q.playerId===player.id&&q.mode===gameMode)}catch{}dtSave();dtClear();dtHistoryRender()}
 const blocked=dtBusy||modeChanging||Boolean(pending)||autoActive()||free>0||!dtBetting()||Boolean(player&&!ready);
 $('dtBalance').textContent=player?fmt(balance):'—';$('dtBalanceUnit').textContent=gameMode==='LOBBY'?'Gold':'chips';$('dtCurrency').textContent=gameMode==='LOBBY'?'LOBBY · GOLD':'LUCKY SEVEN · CHIPS';
 $('dtTotal').textContent=money(dtTotals());$('dtQueueStatus').textContent=dtQueued()?t('Pending')+' +'+money(dtQueued()):'';
 document.querySelectorAll('[data-dt-chip]').forEach(b=>{b.setAttribute('aria-pressed',String(Number(b.dataset.dtChip)===dtChip));b.disabled=dtBusy});
 document.querySelectorAll('[data-dt-side]').forEach(b=>{b.disabled=blocked;b.setAttribute('aria-pressed',String(b.dataset.dtSide===dtSide));b.classList.toggle('placed',dtTotals(b.dataset.dtSide)>0);b.querySelector('.dt-side-total').textContent=t('Your bet')+' '+money(dtTotals(b.dataset.dtSide));b.querySelector('.dt-side-pending').textContent=dtQueued(b.dataset.dtSide)?'+'+money(dtQueued(b.dataset.dtSide))+' · '+t('Pending'):t('Tap again to add more')});
 for(const [id,mode]of[['dtLobby','LOBBY'],['dtClub','CLUB']]){$(id).setAttribute('aria-pressed',String(gameMode===mode));$(id).disabled=dtLocked()||busy||polling||modeChanging||rtpSaving}
 $('dtRetry').hidden=!dtQueue.length||dtSending||!dtError;$('dtStatus').textContent=dtError||(free>0?t('Play Super Ace to finish your free spins first.'):!player?t('Sign in to place a bet'):dtSending?t('Confirming your bet…'):dtBetting()?t('Choose a chip, then tap a side'):t('Your result is saved.'));
}
function dtCard(card){const el=document.createElement('div');el.className='dt-card';const suit=card?({SPADES:'♠',HEARTS:'♥',DIAMONDS:'♦',CLUBS:'♣'})[card.suit]:'',rank=card?({1:'A',11:'J',12:'Q',13:'K'})[card.rank]||String(card.rank):'';el.setAttribute('aria-label',card?rank+' '+card.suit:'Face-down card');const corner='<span>'+rank+'</span><small>'+suit+'</small>';el.innerHTML='<div class="dt-card-inner"><div class="dt-card-back"></div><div class="dt-card-front '+(card&&['HEARTS','DIAMONDS'].includes(card.suit)?'red':'')+'"><span class="dt-card-corner">'+corner+'</span><strong>'+suit+'</strong><span class="dt-card-corner bottom">'+corner+'</span></div></div>';return el}
function dtClear(){for(const side of ['Dragon','Tiger']){$('dt'+side+'Cards').replaceChildren(dtCard());$('dt'+side+'Score').textContent='—';$('dt'+side+'Hand').classList.remove('winner')}$('dtResult').classList.remove('won');$('dtResultTitle').textContent=t('Choose your side');$('dtResultDetail').textContent=t('Higher card wins · A low, K high');document.querySelectorAll('[data-dt-side]').forEach(b=>b.classList.remove('winning'))}
function dtClock(){
 if(!dtTableState||activeView!=='dragonTigerView')return;
 const betting=dtBetting(),end=betting?dtTableState.bettingClosesAt:dtTableState.nextRoundAt,seconds=Math.max(0,Math.ceil((end-dtServerNow())/1000));
 $('dtCountdown').textContent=String(seconds).padStart(2,'0');$('dtPhase').textContent=t(betting?'BETTING OPEN':'CARDS OPEN');$('dtClock').classList.toggle('reveal',!betting);$('dtClock').classList.toggle('urgent',Boolean(betting&&seconds<=3));$('dtClock').style.setProperty('--dt-time',String(betting?Math.max(0,(end-dtServerNow())/10000):0));
 if(seconds!==dtTickSecond){dtTickSecond=seconds;if(betting&&seconds<=3&&!document.hidden)dtCue('tick')}
 if(!dtBusy&&!dtSending&&!dtFetching){if(dtServerNow()>=dtTableState.nextRoundAt)void dtFetchTable();else if(!betting&&dtShownRound!==dtTableState.roundId)void dtReveal();else if(Date.now()-dtLastSync>2000)void dtFetchTable()}
 dtUpdate();
}
async function dtOwnBets(id){if(!player)return [];return api('dragon-tiger/bets?mode='+gameMode+'&roundId='+id)}
async function dtFetchTable(){
 if(dtFetching||dtBusy||dtSending)return;dtFetching=true;const owner=dtKey();
 try{const state=await api('dragon-tiger/table');if(owner!==dtKey())return;dtClockOffset=state.serverTime-Date.now();const changed=dtTableState?.roundId!==state.roundId;dtTableState=state;if(changed){dtShownRound=null;dtLast=null;dtClear()}
  const bets=await dtOwnBets(state.roundId);if(owner!==dtKey()||dtSending)return;dtBets=bets;dtLastSync=Date.now();if(player)applyWallet(await api('me'));if(dtQueue.length&&!dtSending)void dtDrain();
 }catch(e){dtError=errorText(e)}finally{dtFetching=false;dtUpdate()}
}
async function dtLoad(){dtUpdate();await dtFetchTable();await dtHistory();dtClock()}
function dtChipEffect(side,value){const box=document.querySelector('[data-dt-side='+side+']');box.classList.remove('chip-hit');void box.offsetWidth;box.classList.add('chip-hit');const el=document.createElement('span');el.className='dt-chip-pop';el.textContent='+'+value;el.setAttribute('aria-hidden','true');box.append(el);setTimeout(()=>el.remove(),700);dtCue('chip')}
function dtPlay(side){
 if(!player){showLogin();return}if(!dtBetting()||dtBusy||modeChanging||pending||autoActive()||free>0||!ready)return;
 if(dtQueue.length>=50)return;
 if(Math.round(balance*100)-dtReserved()<dtChip*100){dtError=errorText({code:'INSUFFICIENT_FUNDS'});dtUpdate();return}
 openAudio();dtSide=side;dtError='';dtQueue.push({playerId:player.id,mode:gameMode,body:{requestId:requestUuid(),tableRoundId:dtTableState.roundId,side,betCents:dtChip*100}});dtSave();dtUpdate();void dtDrain();
}
async function dtDrain(){
 if(dtSending||dtBusy||!dtQueue.length||!ready)return;dtSending=true;busy=true;const owner=dtOwner;dtError='';
 try{while(dtQueue.length&&owner===dtKey()){
  const req=dtQueue[0];if(req.body.expectedRevision===undefined){applyWallet(await api('me?mode='+req.mode));req.body.expectedRevision=revision;dtSave()}
  try{const receipt=await api('dragon-tiger/rounds?mode='+req.mode,'POST',req.body);if(owner!==dtKey())break;dtQueue.shift();dtSave();if(receipt.tableRoundId===dtTableState?.roundId){if(!dtBets.some(b=>b.requestId===receipt.requestId))dtBets.push(receipt);dtChipEffect(receipt.side,receipt.betCents/100)}applyWallet(await api('me?mode='+req.mode));dtUpdate()}
  catch(e){if(e.code==='STALE_STATE'){applyWallet(await api('me?mode='+req.mode));req.body.expectedRevision=revision;dtSave();continue}if(e.status>=400&&e.status<500){dtQueue.shift();dtSave();dtError=errorText(e);if(e.status===401){ready=false;showLogin();break}continue}dtError=t('Connection lost. Retry uses the same request and cannot charge twice.');break}
 }}catch(e){dtError=errorText(e)}finally{dtSending=false;busy=false;dtSave();update()}
}
function dtPause(ms){if(document.hidden||matchMedia('(prefers-reduced-motion: reduce)').matches)return Promise.resolve();return new Promise(resolve=>{const done=()=>{clearTimeout(timer);dtWaiters.delete(done);resolve()},timer=setTimeout(done,ms);dtWaiters.add(done)})}
function dtShowFinal(row){dtLast=row;const o=row.outcome;for(const side of ['Dragon','Tiger']){$('dt'+side+'Cards').replaceChildren(...o[side.toLowerCase()].map(c=>{const el=dtCard(c);el.classList.add('revealed');return el}));$('dt'+side+'Score').textContent=o[side.toLowerCase()+'Score'];$('dt'+side+'Hand').classList.toggle('winner',o.winner===side.toUpperCase()||o.winner==='TIE')}
 document.querySelectorAll('[data-dt-side]').forEach(b=>b.classList.toggle('winning',b.dataset.dtSide===o.winner));$('dtResultTitle').textContent=t(o.winner==='TIE'?'Tie game':o.winner==='DRAGON'?'Dragon wins':'Tiger wins');const net=row.payoutCents-row.betCents;$('dtResultDetail').textContent=row.betCents?t(net>0?'You won':net===0?'Stake returned':'No win this round')+' · '+t('Net')+' '+(net>0?'+':'')+money(net)+' · '+t('Payout')+' '+money(row.payoutCents):t('No bet placed · Table result');$('dtResult').classList.toggle('won',net>0)
}
async function dtAnimate(row){dtClear();$('dtResultTitle').textContent=t('Dealing cards…');for(const side of ['Dragon','Tiger'])$('dt'+side+'Cards').replaceChildren(...row.outcome[side.toLowerCase()].map(dtCard));const d=[...$('dtDragonCards').children],g=[...$('dtTigerCards').children],cards=[d[0],g[0],d[1],g[1]].filter(Boolean);cards.forEach(e=>e.style.visibility='hidden');for(const e of cards){e.style.visibility='';e.classList.add('flying');dtCue('deal');await dtPause(120)}for(const e of cards){e.classList.add('revealed');dtCue('flip');await dtPause(220)}dtShowFinal(row);if(row.payoutCents>row.betCents){dtCue('win');if(!document.hidden&&!matchMedia('(prefers-reduced-motion: reduce)').matches){const r=$('dtResult').getBoundingClientRect();burst(r.x+r.width/2,r.y+r.height/2,22,true)}}}
async function dtReveal(){
 if(dtBusy||dtSending||dtFetching||!dtTableState||dtBetting())return;
 dtBusy=true;busy=true;const id=dtTableState.roundId,owner=dtOwner;
 try{const state=await api('dragon-tiger/table');if(state.roundId!==id||!state.outcome)return;const bets=await dtOwnBets(id);if(owner!==dtKey())return;dtBets=bets;dtShownRound=id;const row={tableRoundId:id,outcome:state.outcome,betCents:bets.reduce((n,b)=>n+b.betCents,0),payoutCents:bets.reduce((n,b)=>n+b.payoutCents,0)};if(player)applyWallet(await api('me'));await dtAnimate(row);await dtHistory()}
 catch(e){dtShownRound=null;dtError=errorText(e)}finally{dtBusy=false;busy=false;update()}
}
function dtHistoryRender(){
 const rounds=dtTableRows.slice(0,20);
 $('dtRoad').replaceChildren(...rounds.slice().reverse().map(row=>{const el=document.createElement('span');el.className='dt-bead '+row.outcome.winner;el.textContent=row.outcome.winner==='DRAGON'?'D':row.outcome.winner==='TIGER'?'T':'=';el.title=t('Game')+' #'+row.tableRoundId+' · '+row.outcome.dragonScore+' : '+row.outcome.tigerScore;el.setAttribute('aria-label',el.title+' '+row.outcome.winner);return el}));
 $('dtHistoryList').innerHTML=rounds.length?rounds.map(row=>{
  const bets=dtRows.filter(b=>b.tableRoundId===row.tableRoundId),stake=bets.reduce((n,b)=>n+b.betCents,0),payout=bets.reduce((n,b)=>n+b.payoutCents,0);
  const sides=['DRAGON','TIGER','TIE'].map(side=>{const value=bets.filter(b=>b.side===side).reduce((n,b)=>n+b.betCents,0);return value?esc(side==='TIE'?t('TIE'):side)+' '+money(value):''}).filter(Boolean).join(' · ');
  return '<div class="dt-history-row" data-round-id="'+row.tableRoundId+'"><span><b>'+esc(row.outcome.winner==='TIE'?t('TIE'):row.outcome.winner)+'</b> '+row.outcome.dragonScore+' : '+row.outcome.tigerScore+'<small>#'+row.tableRoundId+' · '+esc(stamp(row.createdAt))+'</small></span><span>'+t('Bet')+' '+money(stake)+'<small>'+(sides||t('No bet'))+'</small></span><span>'+(bets.length?netCell(payout-stake):'—')+'<small>'+t('Payout')+' '+money(payout)+'</small></span></div>';
 }).join(''):'<p class="dt-history-note">'+t('No completed games yet.')+'</p>';
}
async function dtHistory(){const owner=dtKey(),seq=++dtHistoryVersion;try{const [table,rows]=await Promise.all([api('dragon-tiger/table/history'),player?api('dragon-tiger/rounds?mode='+gameMode):Promise.resolve([])]);if(owner===dtKey()&&seq===dtHistoryVersion){dtTableRows=table;dtRows=rows;dtHistoryRender()}}catch{if(owner===dtKey()&&seq===dtHistoryVersion)$('dtHistoryList').textContent=t('History unavailable. Tap Refresh.')}}
document.querySelectorAll('[data-dt-side]').forEach(b=>{const total=document.createElement('strong');total.className='dt-side-total';total.setAttribute('aria-live','polite');const note=document.createElement('small');note.className='dt-side-pending';b.append(total,note);b.onclick=()=>dtPlay(b.dataset.dtSide)});
document.querySelectorAll('[data-dt-chip]').forEach(b=>b.onclick=()=>{dtChip=Number(b.dataset.dtChip);openAudio();dtCue('select');dtUpdate()});
$('dtRetry').onclick=()=>dtDrain();$('dtLobby').onclick=()=>switchMode('LOBBY');$('dtClub').onclick=()=>switchMode('CLUB');$('dtBack').onclick=e=>{e.preventDefault();void showView('gameView')};$('dtHistoryRefresh').onclick=()=>dtLoad();$('dtRulesButton').onclick=()=>$('dtRules').showModal();$('dtRulesClose').onclick=()=>$('dtRules').close();
document.addEventListener('visibilitychange',()=>{if(document.hidden)for(const done of [...dtWaiters])done();else if(activeView==='dragonTigerView')void dtLoad()});
const dtPreviousHelp=$('help').onclick;$('help').onclick=()=>activeView==='dragonTigerView'?$('dtRules').showModal():dtPreviousHelp();const dtPreviousLanguage=setLanguage;setLanguage=value=>{dtPreviousLanguage(value);dtHistoryRender();if(dtLast)dtShowFinal(dtLast);dtUpdate()};dtClear();dtUpdate();localize();setInterval(()=>dtClock(),150);
