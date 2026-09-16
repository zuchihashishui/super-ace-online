'use strict';
Object.assign(english,{
 dtRulesCards:'Each round uses a freshly shuffled 52-card deck. Two cards are dealt to Dragon and two to Tiger, without replacement. No third card is drawn.',
 dtRulesScore:'A = 1; 2–9 = face value; 10, J, Q, K = 0. Add the two cards and keep the last digit. The higher score wins; equal scores are a Tie.',
 dtRulesPay:'Choose one side per round. Dragon / Tiger win: total return 1.95× your stake (profit 0.95×). Tie win: total return 9× (profit 8×). A tie refunds Dragon / Tiger stakes in full. Other losing bets return 0. Returns are rounded to the nearest 0.01.',
 dtRulesSeparate:'This is an independent two-card variant, not standard one-card Dragon Tiger or full Baccarat. It has fixed card probabilities and payouts. Super Ace RTP settings do not apply to this game.',
 dtRulesExample:'Example: Dragon 8 + 7 = 5 points; Tiger A + 3 = 4 points. Dragon wins. Betting 5 on Dragon returns 9.75, including the original 5.'
});
Object.assign(filipino,{
 'Paid rounds':'Mga bayad na laro',
 'Two-card duel':'Laban ng dalawang baraha','Two cards. One winning side.':'Dalawang baraha. Isang panalong panig.','Last digit wins':'Huling digit ang puntos','Choose your side':'Piliin ang iyong panig','Higher score wins · 0–9 points':'Mas mataas na puntos ang panalo · 0–9','BET ON':'TUMAYA SA','TIE':'TABLA','Return ×1.95':'Balik ×1.95','Return ×9':'Balik ×9','Returns include your stake. Dragon / Tiger bets are refunded on a tie.':'Kasama ang taya sa balik. Ibinabalik ang taya sa Dragon / Tiger kapag tabla.','Available balance':'Magagamit na balanse','Deal cards':'Mamigay ng baraha','YOUR TABLE':'IYONG MESA','Recent rounds':'Mga nakaraang laro','Your last 50 rounds in this mode. Past results do not predict the next round.':'Huling 50 laro mo sa mode na ito. Hindi hinuhulaan ng nakaraan ang susunod na resulta.','Independent two-card variant · Virtual chips only':'Hiwalay na bersiyong dalawang baraha · Virtual chips lamang','Finish or recover the current round first.':'Tapusin o bawiin muna ang kasalukuyang laro.','Confirming your bet…':'Kinukumpirma ang iyong taya…','Dealing cards…':'Namimigay ng baraha…','Dragon wins':'Panalo ang Dragon','Tiger wins':'Panalo ang Tiger','Tie game':'Tabla ang laro','Stake returned':'Ibinalik ang taya','You won':'Nanalo ka','No win this round':'Hindi nanalo sa larong ito','Retry same round':'Ulitin ang parehong laro','Your result is saved.':'Naka-save ang iyong resulta.','History unavailable. Tap Refresh.':'Hindi makuha ang kasaysayan. Pindutin ang Refresh.','No rounds yet. Choose a side to begin.':'Wala pang laro. Pumili ng panig upang magsimula.','Sign in to place a bet':'Mag-sign in upang tumaya','Play Super Ace to finish your free spins first.':'Laruin muna ang Super Ace upang tapusin ang libreng ikot.',
 dtRulesCards:'Bawat laro ay may bagong binulasang 52 baraha. Dalawang baraha ang ibibigay sa Dragon at dalawa sa Tiger, walang ibinabalik sa deck. Walang ikatlong baraha.',
 dtRulesScore:'A = 1; 2–9 = halaga ng baraha; 10, J, Q, K = 0. Pagsamahin ang dalawa at kunin ang huling digit. Mas mataas ang panalo; magkapareho ay tabla.',
 dtRulesPay:'Pumili ng isang panig bawat laro. Panalo sa Dragon / Tiger: kabuuang balik 1.95× ng taya (tubo 0.95×). Panalo sa Tabla: balik 9× (tubo 8×). Kung tabla, buong taya sa Dragon / Tiger ay ibabalik. Walang balik sa ibang talong taya. Niro-round sa pinakamalapit na 0.01 ang balik.',
 dtRulesSeparate:'Hiwalay itong bersiyong dalawang baraha, hindi karaniwang isang-barahang Dragon Tiger o buong Baccarat. Nakatakda ang tsansa at bayad. Hindi naaangkop dito ang RTP settings ng Super Ace.',
 dtRulesExample:'Halimbawa: Dragon 8 + 7 = 5 puntos; Tiger A + 3 = 4 puntos. Panalo ang Dragon. Ang tayang 5 sa Dragon ay may balik na 9.75, kasama ang orihinal na 5.'
});
let dtBusy=false,dtPending=null,dtSide='DRAGON',dtOwner='',dtRows=[],dtLast=null,dtHistorySequence=0;
const dtWaiters=new Set();
function dtLocked(){return dtBusy||Boolean(dtPending);}
function dtKey(){return player?player.id+':'+gameMode:'';}
function dtSave(value){dtPending=value;try{if(value)sessionStorage.setItem('dt-pending-'+dtOwner,JSON.stringify(value));else if(dtOwner)sessionStorage.removeItem('dt-pending-'+dtOwner)}catch{}}
function dtCard(card){
 const el=document.createElement('div');el.className='dt-card';
 const suit=card?({SPADES:'♠',HEARTS:'♥',DIAMONDS:'♦',CLUBS:'♣'})[card.suit]:'';
 const rank=card?({1:'A',11:'J',12:'Q',13:'K'})[card.rank]||String(card.rank):'';
 el.setAttribute('aria-label',card?rank+' '+card.suit:'Face-down card');
 const corner='<span>'+rank+'</span><small>'+suit+'</small>';
 el.innerHTML='<div class="dt-card-inner"><div class="dt-card-back"></div><div class="dt-card-front '+(card&&['HEARTS','DIAMONDS'].includes(card.suit)?'red':'')+'"><span class="dt-card-corner">'+corner+'</span><strong>'+suit+'</strong><span class="dt-card-corner bottom">'+corner+'</span></div></div>';return el;
}
function dtClear(){
 for(const side of ['Dragon','Tiger']){$('dt'+side+'Cards').replaceChildren(dtCard(),dtCard());$('dt'+side+'Score').textContent='—';$('dt'+side+'Hand').classList.remove('winner')}
 $('dtResult').classList.remove('won');$('dtResultTitle').textContent=t('Choose your side');$('dtResultDetail').textContent=t('Higher score wins · 0–9 points');
}
function dtUpdate(){
 if(!$('dtBet'))return;
 const key=dtKey();
 if(key!==dtOwner&&!dtBusy){dtOwner=key;dtPending=null;dtLast=null;dtRows=[];dtHistorySequence++;dtClear();try{const saved=JSON.parse(sessionStorage.getItem('dt-pending-'+key)||'null');if(key&&saved?.mode===gameMode&&saved?.playerId===player.id){dtPending=saved;dtSide=saved.body.side;$('dtBet').value=String(saved.body.betCents/100)}}catch{}dtHistoryRender();if(dtPending&&activeView!=='dragonTigerView')queueMicrotask(()=>{if(dtPending&&!dtBusy)void showView('dragonTigerView')});}
 const locked=dtLocked()||busy||polling||modeChanging||Boolean(pending)||autoActive()||free>0;
 $('dtBalance').textContent=player?fmt(balance):'—';$('dtBalanceUnit').textContent=gameMode==='LOBBY'?'Gold':'chips';
 $('dtCurrency').textContent=gameMode==='LOBBY'?'LOBBY · GOLD':'LUCKY SEVEN · CHIPS';
 for(const [id,mode]of[['dtLobby','LOBBY'],['dtClub','CLUB']]){$(id).setAttribute('aria-pressed',String(gameMode===mode));$(id).disabled=locked||rtpSaving}
 $('dtBet').disabled=locked||!ready;
 $('dtMinus').disabled=locked||!ready;$('dtPlus').disabled=locked||!ready;
 document.querySelectorAll('[data-dt-side]').forEach(b=>{b.disabled=locked;b.setAttribute('aria-pressed',String(b.dataset.dtSide===dtSide))});
 $('dtDeal').disabled=dtBusy||busy||polling||modeChanging||Boolean(pending)||autoActive()||free>0||Boolean(player&&!ready);
 $('dtDeal').querySelector('span').textContent=t(!player?'Sign in to place a bet':dtBusy?'Dealing cards…':dtPending?'Retry same round':'Deal cards');
 if(!dtBusy){if(dtPending)$('dtStatus').textContent=t('Connection lost. Retry uses the same request and cannot charge twice.');else if(free>0)$('dtStatus').textContent=t('Play Super Ace to finish your free spins first.');else if(autoActive())$('dtStatus').textContent=t('Stop autoplay first.');}
}
function dtHistoryRender(){
 $('dtRoad').replaceChildren(...dtRows.slice().reverse().map(row=>{const e=document.createElement('span');e.className='dt-bead '+row.outcome.winner;e.textContent=row.outcome.winner==='DRAGON'?'D':row.outcome.winner==='TIGER'?'T':'=';e.title=row.outcome.dragonScore+' : '+row.outcome.tigerScore;e.setAttribute('aria-label',row.outcome.winner+' '+e.title);return e}));
 $('dtHistoryList').innerHTML=dtRows.length?dtRows.slice(0,5).map(row=>'<div class="dt-history-row"><span>'+esc(row.outcome.winner==='TIE'?t('TIE'):row.outcome.winner)+' <small>'+row.outcome.dragonScore+' : '+row.outcome.tigerScore+' · '+esc(stamp(row.createdAt))+'</small></span><span>'+esc(row.side==='TIE'?t('TIE'):row.side)+'<small>'+esc(t('Bet'))+' '+money(row.betCents)+'</small></span><span>'+netCell(row.payoutCents-row.betCents)+'<small>'+esc(t('Payout'))+' '+money(row.payoutCents)+'</small></span></div>').join(''):'<p class="dt-history-note">'+esc(t('No rounds yet. Choose a side to begin.'))+'</p>';
}
async function dtLoad(){
 dtUpdate();const key=dtOwner,seq=++dtHistorySequence;if(!player)return;
 try{const rows=await api('dragon-tiger/rounds?mode='+gameMode);if(key!==dtKey()||seq!==dtHistorySequence)return;dtRows=rows;dtHistoryRender();if(!dtBusy&&!dtPending&&!dtLast&&rows.length)dtShowFinal(rows[0]);}
 catch(e){if(key===dtKey()&&seq===dtHistorySequence)$('dtHistoryList').textContent=t('History unavailable. Tap Refresh.');}
}
function dtPause(ms){if(document.hidden||matchMedia('(prefers-reduced-motion: reduce)').matches)return Promise.resolve();return new Promise(resolve=>{const done=()=>{clearTimeout(timer);dtWaiters.delete(done);resolve()};const timer=setTimeout(done,ms);dtWaiters.add(done)})}
document.addEventListener('visibilitychange',()=>{if(document.hidden)for(const done of [...dtWaiters])done()});
function dtShowFinal(row){
 dtLast=row;const o=row.outcome;
 for(const side of ['Dragon','Tiger']){
  $('dt'+side+'Cards').replaceChildren(...o[side.toLowerCase()].map(c=>{const el=dtCard(c);el.classList.add('revealed');return el}));
  $('dt'+side+'Score').textContent=o[side.toLowerCase()+'Score'];
  $('dt'+side+'Hand').classList.toggle('winner',o.winner===side.toUpperCase()||o.winner==='TIE');
 }
 const net=row.payoutCents-row.betCents;
 $('dtResultTitle').textContent=t(o.winner==='TIE'?'Tie game':o.winner==='DRAGON'?'Dragon wins':'Tiger wins');
 $('dtResultDetail').textContent=t(net>0?'You won':net===0?'Stake returned':'No win this round')+' · '+t('Net')+' '+(net>0?'+':'')+money(net)+' · '+t('Payout')+' '+money(row.payoutCents);
 $('dtResult').classList.toggle('won',net>0);
}
async function dtAnimate(row){
 dtClear();$('dtResultTitle').textContent=t('Dealing cards…');
 for(const side of ['Dragon','Tiger'])$('dt'+side+'Cards').replaceChildren(...row.outcome[side.toLowerCase()].map(dtCard));
 const d=[...$('dtDragonCards').children],tg=[...$('dtTigerCards').children],cards=[d[0],tg[0],d[1],tg[1]];
 cards.forEach(el=>el.style.visibility='hidden');
 for(const el of cards){el.style.visibility='';el.classList.add('flying');tone(260,.06,'triangle');await dtPause(150)}
 await dtPause(180);
 for(const el of cards){el.classList.add('revealed');tone(420,.06,'sine');await dtPause(250)}
 await dtPause(350);dtShowFinal(row);
 if(row.payoutCents>row.betCents){winChime(row.side==='TIE'?2:0);if(!document.hidden&&!matchMedia('(prefers-reduced-motion: reduce)').matches){const rect=$('dtResult').getBoundingClientRect();burst(rect.x+rect.width/2,rect.y+rect.height/2,row.side==='TIE'?32:16,true)}}
}
async function dtPlay(event){
 event?.preventDefault();if(!player){showLogin();return}
 if(dtBusy||busy||polling||modeChanging||pending||autoActive()||free>0||!ready)return;
 const input=$('dtBet');
 if(!dtPending&&(!input.reportValidity()||!Number.isFinite(input.valueAsNumber)))return;
 openAudio();
 if(!dtPending)dtSave({playerId:player.id,mode:gameMode,body:{requestId:requestUuid(),side:dtSide,betCents:Math.round(input.valueAsNumber*100),expectedRevision:revision}});
 const req=dtPending,owner=dtOwner;dtBusy=true;busy=true;let result=null;
 $('dtStatus').textContent=t('Confirming your bet…');update();
 try{
  result=await api('dragon-tiger/rounds?mode='+req.mode,'POST',req.body);
  if(owner!==dtKey())return;
  balance=result.balanceCents/100;revision=result.revision;
  await dtAnimate(result);dtSave(null);
  $('dtStatus').textContent=t('Your result is saved.');
 }catch(e){
  if(owner!==dtKey())return;
  if(result){dtShowFinal(result);dtSave(null);$('dtStatus').textContent=t('Your result is saved.');}
  else if(e.status>=400&&e.status<500){dtSave(null);$('dtStatus').textContent=errorText(e);if(e.status===401){ready=false;showLogin()}}
  else $('dtStatus').textContent=t('Connection lost. Retry uses the same request and cannot charge twice.');
 }finally{
  if(owner===dtKey())try{applyWallet(await api('me?mode='+req.mode))}catch{}
  dtBusy=false;busy=false;update();if(owner===dtKey())await dtLoad();
 }
 return result;
}
$('dtBetForm').onsubmit=dtPlay;
document.querySelectorAll('[data-dt-side]').forEach(b=>b.onclick=()=>{if(!b.disabled){dtSide=b.dataset.dtSide;dtUpdate()}});
for(const [id,delta]of[['dtMinus',-5],['dtPlus',5]])$(id).onclick=()=>{const input=$('dtBet');if(!input.disabled&&input.reportValidity())input.value=String(Math.min(500,Math.max(5,Math.round((input.valueAsNumber+delta)*100)/100)))};
$('dtLobby').onclick=()=>switchMode('LOBBY');$('dtClub').onclick=()=>switchMode('CLUB');
$('dtBack').onclick=e=>{e.preventDefault();void showView('gameView')};
$('dtHistoryRefresh').onclick=()=>dtLoad();
$('dtRulesButton').onclick=()=>$('dtRules').showModal();$('dtRulesClose').onclick=()=>$('dtRules').close();
const dtPreviousHelp=$('help').onclick;$('help').onclick=()=>activeView==='dragonTigerView'?$('dtRules').showModal():dtPreviousHelp();
const dtPreviousLanguage=setLanguage;setLanguage=value=>{dtPreviousLanguage(value);dtHistoryRender();if(dtLast)dtShowFinal(dtLast);dtUpdate()};
dtClear();dtUpdate();localize();
