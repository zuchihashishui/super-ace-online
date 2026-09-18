'use strict';
Object.assign(filipino,{'completed games':'natapos na laro','dice':'dice','Newest first':'Pinakabago muna','Round':'Laro'});
Object.assign(english,{cgJackpotPending:'Jackpot is not active. Its winning conditions and prize rules have not been configured. Regular Color Game payouts are unchanged.'});
Object.assign(filipino,{'Jackpot not active':'Hindi pa aktibo ang jackpot','Daily returns · includes stakes':'Balik ngayong araw · kasama ang puhunan','Table total':'Kabuuang taya sa mesa',cgJackpotPending:'Hindi pa aktibo ang jackpot. Hindi pa itinakda ang kondisyon ng panalo at mga patakaran sa premyo. Hindi nagbago ang karaniwang bayad ng Color Game.'});
function cgCompact(cents){const amount=cents/100;return amount>=1000000?(amount/1000000).toFixed(amount%1000000?2:0)+'M':amount>=10000?(amount/1000).toFixed(amount%1000?1:0)+'K':money(cents);}

Object.assign(english,{cgRules:'Three independent dice, six equally likely colors. Select a chip and tap up to three different colors per round during the 10-second betting window. Each tap adds a bet. Accepted bets cannot be cancelled. If your color appears once / twice / three times, the total return is 2× / 3× / 4× the stake, including the stake. No match returns zero. Lobby Jackpot rules are available using the ? button above the pool. Super Ace RTP settings do not apply.',cgHistoryNote:'Last 20 completed games. Percentages describe past dice, not the next result.'});
Object.assign(filipino,{'Choose a chip, then tap a color':'Pumili ng chip, saka pindutin ang kulay','Choose your color':'Piliin ang iyong kulay','Color Game':'Color Game','YELLOW':'DILAW','WHITE':'PUTI','PINK':'ROSAS','BLUE':'ASUL','RED':'PULA','GREEN':'BERDE','Three dice. Six colors.':'Tatlong dice. Anim na kulay.','Daily returns':'Mga balik ngayong araw','No payouts yet today.':'Wala pang balik ngayong araw.','Total returned · includes stakes':'Kabuuang ibinalik · kasama ang taya','Three matches':'Tatlong tugma','Gross return':'Kabuuang balik','Rolling…':'Gumugulong…','Waiting for next round':'Naghihintay ng susunod na laro','1 match':'1 tugma','2 matches':'2 tugma','3 matches':'3 tugma','Color history':'Kasaysayan ng kulay','History not available yet.':'Hindi pa makuha ang kasaysayan.',cgRules:'Tatlong hiwalay na dice na may anim na kulay at pantay na pagkakataon. Pumili ng chip at tumaya sa hanggang tatlong magkaibang kulay bawat laro sa loob ng 10 segundo. Bawat pindot ay dagdag na taya. Hindi maaaring kanselahin ang tinanggap na taya. Kapag lumabas ang kulay nang isa / dalawa / tatlong beses, ang kabuuang balik ay 2× / 3× / 4× kasama ang puhunan. Walang tugma: walang balik. Makikita ang mga patakaran ng Lobby Jackpot sa ? na pindutan sa itaas ng pondo. Hindi naaangkop ang RTP ng Super Ace.',cgHistoryNote:'Huling 20 natapos na laro. Ang porsiyento ay mula sa nakaraang dice, hindi hula sa susunod.'});
Object.assign(filipino,{'Maximum 3 colors per round.':'Hanggang 3 kulay lamang bawat laro.','Colors selected':'Napiling kulay','Total bet':'Kabuuang taya'});
const cgColors=['YELLOW','WHITE','PINK','BLUE','RED','GREEN'],cgSymbols=['☀','◆','♥','●','★','♣'];
let cgState=null,cgOffset=0,cgOwner='',cgBets=[],cgQueue=[],cgChip=5,cgSending=false,cgFetching=false,cgBusy=false,cgRows=[],cgHistoryRows=[],cgCrowd=null,cgShown=null,cgLastFetch=0,cgError='',cgTick=-1,cgHistoryAt=0,cgStamp='',cgGeneration=0,cgMutation=0,cgHistoryVersion=0,cgHistoryPage=0,cgHistorySelected=null,cgNetworkError='',cgRevealTimer=null;
function cgKey(){return (player?.id||'guest')+':'+gameMode}
function cgNow(){return Date.now()+cgOffset}
function cgBetting(){return cgState&&Date.now()-cgLastFetch<4500&&cgNow()<cgState.bettingClosesAt}
function cgLocked(){return cgSending||cgBusy||cgQueue.length>0||cgBets.some(b=>!b.outcome)}
function cgSave(){try{if(player&&cgOwner===cgKey())sessionStorage.setItem('cg-queue-'+cgOwner,JSON.stringify(cgQueue))}catch{}}
function cgReset(){if(cgOwner===cgKey())return;cgOwner=cgKey();cgGeneration++;cgMutation++;cgHistoryVersion++;clearTimeout(cgRevealTimer);cgRevealTimer=null;cgStopDice();cgBusy=false;$('cgCase').classList.remove('rolling');cgLastFetch=0;cgNetworkError='';cgBets=[];cgQueue=[];cgRows=[];cgHistoryRows=[];cgHistoryPage=0;cgHistorySelected=null;cgHistoryRender();cgCrowd=null;cgShown=null;cgState=null;cgError='';cgStamp='';cgHistoryAt=0;try{if(player)cgQueue=JSON.parse(sessionStorage.getItem('cg-queue-'+cgOwner)||'[]').filter(q=>q.playerId===player.id&&q.mode===gameMode)}catch{}cgDice();}
function cgSelected(){return new Set([...cgBets.filter(b=>b.tableRoundId===cgState?.roundId).map(b=>b.side),...cgQueue.filter(q=>q.body.tableRoundId===cgState?.roundId).map(q=>q.body.side)])}
function cgAmount(side){return cgBets.filter(b=>b.tableRoundId===cgState?.roundId&&(!side||b.side===side)).reduce((n,b)=>n+b.betCents,0)}
function cgQueued(side){return cgQueue.filter(q=>!side||q.body.side===side).reduce((n,q)=>n+q.body.betCents,0)}
function cgCue(kind){if(activeView!=='colorGameView'||document.hidden||!sound||!volume)return;openAudio();if(kind==='win'){[523,659,784,1047].forEach((n,i)=>tone(n,.15,'triangle',i*.08))}else if(kind==='impact'){[175,120,85].forEach((n,i)=>tone(n,.045,'triangle',i*.085,n*.7))}else if(kind==='roll'){[150,120,95].forEach((n,i)=>tone(n,.06,'triangle',.84+i*.17))}else tone(kind==='tick'?900:kind==='chip'?660:510,.05,'sine')}
let cgAnimations=[],cgFinishReveal=null;
function cgStopDice(){cgAnimations.forEach(a=>a.cancel());cgAnimations=[];cgFinishReveal=null;}
function cgDice(outcome){window.CG3D?.stop();cgStopDice();document.querySelectorAll('.cg-feeder i').forEach((el,i)=>{const c=outcome?.dice[i];el.className=c||'';el.textContent=c?cgSymbols[cgColors.indexOf(c)]:'?';});$('cgDice').replaceChildren(...[0,1,2].map(i=>{const color=outcome?.dice[i],wrap=document.createElement('div');wrap.className='cg-die '+(color||'hidden-die');wrap.setAttribute('aria-label',color?t(color):'Hidden die');const other=cgColors.filter(c=>c!==color),order=color?[...other.slice(0,4),color,other[4]]:cgColors;wrap.innerHTML='<div class="cg-shadow"></div><div class="cg-cube">'+order.map((c,j)=>'<span aria-hidden="true" class="cg-cube-face cf-'+j+(color&&j===4?' result-face':'')+' '+(color?c:'unknown')+'">'+(color?cgSymbols[cgColors.indexOf(c)]:'?')+'</span>').join('')+'</div><small>'+(color?t(color):'')+'</small>';return wrap}));}
function cgUpdate(){
 if(!$('cgTable'))return;cgReset();const betting=cgBetting(),blocked=!betting||cgBusy||busy&&!cgSending||modeChanging||Boolean(pending)||autoActive()||free>0||Boolean(player&&!ready);
 $('cgBalance').textContent=player?fmt(balance):'—';$('cgUnit').textContent=gameMode==='LOBBY'?'Gold':'chips';$('cgTotal').textContent=money(cgAmount());$('cgPending').textContent=cgQueued()?t('Pending')+' +'+money(cgQueued()):'';
 $('cgStatus').textContent=cgNetworkError||cgError||(!player?t('Sign in to place a bet'):free>0?t('Play Super Ace to finish your free spins first.'):cgSending?t('Confirming your bet…'):t(betting?'Choose a chip, then tap a color':'Waiting for next round'));
 $('cgRetry').hidden=!cgQueue.length||cgSending||!cgError;
 for(const b of document.querySelectorAll('[data-cg-chip]')){b.setAttribute('aria-pressed',String(+b.dataset.cgChip===cgChip));b.disabled=cgBusy;}
 for(const [id,mode]of[['cgLobby','LOBBY'],['cgClub','CLUB']]){$(id).setAttribute('aria-pressed',String(gameMode===mode));$(id).disabled=cgLocked()||busy||modeChanging;}
 const signature=cgOwner+':'+cgState?.roundId+':'+language+':'+cgBets.map(b=>b.requestId).join(',');
 for(const b of document.querySelectorAll('[data-cg-side]')){
  const side=b.dataset.cgSide,selected=cgSelected();b.disabled=blocked||(!selected.has(side)&&selected.size>=3);b.classList.toggle('color-limited',!selected.has(side)&&selected.size>=3);b.title=!selected.has(side)&&selected.size>=3?t('Maximum 3 colors per round.'):'';b.classList.toggle('placed',cgAmount(side)>0);b.querySelector('.cg-own').textContent=cgAmount(side)?cgCompact(cgAmount(side)):'';b.querySelector('.cg-own').setAttribute('aria-label',t('Your bet')+' '+money(cgAmount(side)));b.setAttribute('aria-label',t(side)+': '+t('Your bet')+' '+money(cgAmount(side)));b.querySelector('.cg-pending').textContent=cgQueued(side)?'+'+money(cgQueued(side))+' '+t('Pending'):'';
  const pool=cgCrowd?.sides.find(s=>s.side===side);b.querySelector('.cg-others').textContent=t('Others')+' '+(pool?money(pool.othersCents):'—');let total=b.querySelector('.cg-table-total');if(!total){total=document.createElement('strong');total.className='cg-table-total';b.append(total)}total.textContent=pool?cgCompact(pool.ownCents+pool.othersCents):'—';total.setAttribute('aria-label',t('Table total')+' '+(pool?money(pool.ownCents+pool.othersCents):'—'));
  if(signature!==cgStamp){const groups=new Map();cgBets.filter(r=>r.tableRoundId===cgState?.roundId&&r.side===side).forEach(r=>groups.set(r.betCents,(groups.get(r.betCents)||0)+1));b.querySelector('.cg-stacks').innerHTML=[...groups].map(([c,n])=>'<span class="cg-stack" data-cents="'+c+'" data-count="'+n+'" aria-label="'+n+' × '+c/100+'">'+Array.from({length:Math.min(n,4)},(_,i)=>'<i style="--layer:'+i+'">'+c/100+'</i>').join('')+'<small>×'+n+'</small></span>').join('');}
 }
 cgStamp=signature;$('cgBettors').textContent=t('Colors selected')+': '+cgSelected().size+'/3 · '+t('Bettors this round')+': '+(cgCrowd?.bettors??'—');$('cgSound').textContent=sound&&volume?'♫':'♫ ×';
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
function cgResultText(outcome){const rows=cgBets.filter(b=>b.tableRoundId===cgState?.roundId),payout=rows.reduce((n,b)=>n+b.payoutCents,0),bet=cgAmount(),net=payout-bet;$('cgResult').textContent=outcome.dice.map(c=>t(c)).join(' · ')+(bet?' | '+t('Total bet')+' '+money(bet)+' · '+t('Total returned')+' '+money(payout)+' · '+t('Net')+' '+(net>0?'+':'')+money(net):'');if(net>0)cgReturnChips(outcome);return net;}

function cgReveal(outcome){
 if(!window.CG3D){cgRevealCss(outcome);return;}
 const owner=cgOwner,generation=cgGeneration,round=cgState?.roundId;cgBusy=true;clearTimeout(cgRevealTimer);cgDice(outcome);$('cgCase').classList.add('rolling');$('cgResult').textContent=t('Rolling…');let done=false;
 const current=()=>owner===cgKey()&&generation===cgGeneration&&cgState?.roundId===round;
 const finish=()=>{if(done||!current())return;done=true;cgStopDice();cgBusy=false;$('cgCase').classList.remove('rolling');document.querySelectorAll('[data-cg-side]').forEach(b=>b.classList.toggle('winning',outcome.dice.includes(b.dataset.cgSide)));const net=cgResultText(outcome);if(net>0)celebrateWin('colorGameView',round,cgAmount(),cgAmount()+net);cgUpdate();};
 cgFinishReveal=()=>{window.CG3D.finish();finish()};
 window.CG3D.roll(outcome,{instant:document.hidden||matchMedia('(prefers-reduced-motion: reduce)').matches,onImpact:()=>cgCue('impact')}).then(ok=>{if(!current())return;if(ok)finish();else if(!done)cgRevealCss(outcome)}).catch(()=>{if(current()&&!done)cgRevealCss(outcome)});
}
function cgRevealCss(outcome){
 const owner=cgOwner,generation=cgGeneration,round=cgState?.roundId;cgBusy=true;clearTimeout(cgRevealTimer);cgDice(outcome);$('cgCase').classList.add('rolling');$('cgResult').textContent=t('Rolling…');cgCue('roll');
 const finish=()=>{clearTimeout(cgRevealTimer);cgRevealTimer=null;cgStopDice();cgBusy=false;$('cgCase').classList.remove('rolling');if(owner!==cgKey()||generation!==cgGeneration||cgState?.roundId!==round)return;document.querySelectorAll('[data-cg-side]').forEach(b=>b.classList.toggle('winning',outcome.dice.includes(b.dataset.cgSide)));const net=cgResultText(outcome);if(net>0){const stake=cgAmount();celebrateWin('colorGameView',round,stake,stake+net);}cgUpdate();};
 cgFinishReveal=finish;
 const reduced=document.hidden||matchMedia('(prefers-reduced-motion: reduce)').matches;
 if(!reduced){$('cgDice').querySelectorAll('.cg-die').forEach((die,i)=>{const cube=die.querySelector('.cg-cube'),dir=i===1?-1:1,frames=[],shadows=[];
  const arcs=[[0,.40,-245,0],[.40,.60,0,36],[.60,.76,0,15],[.76,.88,0,5]];
  for(let k=0;k<=100;k++){const u=k/100;let y=0;if(u<=.40)y=-245+245*(u/.40)**2;else{const arc=arcs.slice(1).find(a=>u<=a[1]);if(arc){const f=(u-arc[0])/(arc[1]-arc[0]);y=-4*arc[3]*f*(1-f);}}
   const travel=u<.88?1-u/.88:0,spin=u<.40?u/.40*.48:.48+.52*(1-Math.pow(1-(u-.40)/.60,2));
   const rx=-32-1080*(1-spin),ry=24+dir*720*(1-spin),rz=dir*10*Math.sin(u*26)*Math.max(0,1-u/.8);
   frames.push({offset:u,transform:'translate3d('+(-dir*24*travel)+'px,'+y+'px,0) rotateX('+rx+'deg) rotateY('+ry+'deg) rotateZ('+rz+'deg)',opacity:1});
   shadows.push({offset:u,opacity:.08+.32*(1-Math.min(1,Math.abs(y)/245)),transform:'translateX('+(-dir*24*travel)+'px) scale('+(1-.5*Math.min(1,Math.abs(y)/245))+')'});
  }
  cgAnimations.push(cube.animate(frames,{duration:2100,delay:i*65,fill:'both',easing:'linear'}));cgAnimations.push(die.querySelector('.cg-shadow').animate(shadows,{duration:2100,delay:i*65,fill:'both',easing:'linear'}));
 });}

 cgRevealTimer=setTimeout(finish,reduced?0:2250);
}
function cgPlay(side){
 if(!player){showLogin();return}if(!cgBetting()||cgBusy||modeChanging||pending||autoActive()||free>0||!ready)return;if(cgQueue.length>=50)return;const chosen=cgSelected();if(!chosen.has(side)&&chosen.size>=3){cgError=t('Maximum 3 colors per round.');cgUpdate();return;}
 if(Math.round(balance*100)-cgQueued()<cgChip*100){cgError=errorText({code:'INSUFFICIENT_FUNDS'});cgUpdate();return;}
 openAudio();cgError='';cgMutation++;cgQueue.push({playerId:player.id,mode:gameMode,body:{requestId:requestUuid(),tableRoundId:cgState.roundId,side,betCents:cgChip*100}});cgSave();cgUpdate();void cgDrain();
}
async function cgDrain(){
 if(cgSending||cgBusy||!cgQueue.length||!ready)return;cgSending=true;busy=true;const owner=cgOwner;
 try{while(cgQueue.length&&owner===cgKey()){
  const q=cgQueue[0];if(q.body.expectedRevision===undefined){const w=await api('me?mode='+q.mode);if(owner!==cgKey())break;applyWallet(w);q.body.expectedRevision=revision;cgSave();}
  try{const r=await api('color-game/rounds?mode='+q.mode,'POST',q.body);if(owner!==cgKey())break;cgQueue.shift();cgMutation++;cgSave();if(r.tableRoundId===cgState?.roundId&&!cgBets.some(b=>b.requestId===r.requestId))cgBets.push(r);cgCue('chip');cgChipFlight(document.querySelector('[data-cg-chip=+(q.body.betCents/100)+]')||document.querySelector('.cg-picker'),document.querySelector('[data-cg-side=+q.body.side+]'),q.body.betCents/100);const w=await api('me?mode='+q.mode);if(owner!==cgKey())break;applyWallet(w);cgUpdate();}
  catch(e){if(e.code==='STALE_STATE'){const w=await api('me?mode='+q.mode);if(owner!==cgKey())break;applyWallet(w);q.body.expectedRevision=revision;cgSave();continue;}if(e.status===401){cgError=errorText(e);cgAuthFailure(e);break;}if(e.status>=400&&e.status<500){cgQueue.shift();cgMutation++;cgSave();cgError=e.code==='COLOR_LIMIT'?t('Maximum 3 colors per round.'):errorText(e);continue;}cgError=t('Connection lost. Retry uses the same request and cannot charge twice.');break;}
 }}catch(e){if(owner===cgKey()){cgError=errorText(e);cgAuthFailure(e);}}finally{cgSending=false;busy=false;cgSave();cgUpdate();}
}
async function cgHistory(){
 const owner=cgKey(),mode=gameMode,version=++cgHistoryVersion;cgHistoryAt=Date.now();
 try{const [rows,bets,winners]=await Promise.all([api('color-game/table/history?mode='+mode),player?api('color-game/rounds?mode='+mode):[],api('color-game/winners?mode='+mode)]);if(owner!==cgKey()||version!==cgHistoryVersion)return;cgHistoryRows=rows;cgRows=bets;cgHistoryRender();$('cgWinners').innerHTML=winners.length?winners.map((r,i)=>'<li><b>'+String(i+1)+'</b><i class="cg-avatar" aria-hidden="true" style="--avatar-hue:'+((i*73+325)%360)+'">'+esc(r.displayName.slice(0,1).toUpperCase())+'</i><span>'+esc(r.displayName)+'</span><strong><i aria-hidden="true">◉</i> '+money(r.payoutCents)+'</strong></li>').join(''):'<li class="cg-empty">'+t('No payouts yet today.')+'</li>';}catch{if(owner===cgKey()&&version===cgHistoryVersion){$('cgHistoryList').textContent=t('History not available yet.');$('cgHistoryDetail').textContent='';$('cgWinners').textContent=t('History not available yet.');}}
}
function cgHistoryRender(){
 const rows=cgHistoryRows.slice(0,20),dice=rows.flatMap(r=>r.outcome.dice);cgHistoryPage=Math.min(cgHistoryPage,Math.max(0,Math.ceil(rows.length/10)-1));
 $('cgFrequency').innerHTML=cgColors.map(c=>'<div class="cg-frequency-card '+c+'"><span class="cg-swatch '+c+'" role="img" aria-label="'+t(c)+'"></span><b>'+t(c)+'</b><small>'+(dice.length?(dice.filter(v=>v===c).length*100/dice.length).toFixed(2)+'%':'—')+'</small></div>').join('');
 const page=rows.slice(cgHistoryPage*10,cgHistoryPage*10+10);
 if(!page.some(r=>String(r.tableRoundId)===String(cgHistorySelected)))cgHistorySelected=page[0]?.tableRoundId??null;
 $('cgHistoryList').innerHTML=page.length?page.map((r,i)=>'<button type="button" class="cg-history-row'+(cgHistoryPage===0&&i===0?' latest':'')+'" data-history-id="'+esc(String(r.tableRoundId))+'" aria-pressed="'+(String(r.tableRoundId)===String(cgHistorySelected))+'" aria-label="'+t('Round')+' '+String(cgHistoryPage*10+i+1)+': '+r.outcome.dice.map(c=>t(c)).join(', ')+'"><span class="cg-mini-dice">'+r.outcome.dice.map(c=>'<i class="cg-swatch '+c+'" title="'+t(c)+'" aria-label="'+t(c)+'"></i>').join('')+'</span><b>'+String(cgHistoryPage*10+i+1)+'</b></button>').join(''):'<p class="cg-history-empty">'+t('No completed games yet.')+'</p>';
 $('cgHistorySample').textContent=rows.length+' '+t('completed games')+' · '+dice.length+' '+t('dice')+' · '+t('Newest first');
 $('cgHistoryPage').textContent=page.length?String(cgHistoryPage*10+1)+'–'+String(cgHistoryPage*10+page.length)+' / '+String(rows.length):'0 / 0';
 $('cgHistoryPrev').disabled=cgHistoryPage===0;$('cgHistoryNext').disabled=(cgHistoryPage+1)*10>=rows.length;
 cgHistoryDetail();
 $('cgRecent').innerHTML=rows.slice(0,1).map(r=>'<span>'+r.outcome.dice.map(c=>'<i class="cg-swatch '+c+'" title="'+t(c)+'">'+cgSymbols[cgColors.indexOf(c)]+'</i>').join('')+'</span>').join('');
}
function cgHistoryDetail(){
 const r=cgHistoryRows.find(r=>String(r.tableRoundId)===String(cgHistorySelected));if(!r){$('cgHistoryDetail').textContent='';return;}
 const bets=cgRows.filter(b=>b.tableRoundId===r.tableRoundId),stake=bets.reduce((n,b)=>n+b.betCents,0),payout=bets.reduce((n,b)=>n+b.payoutCents,0);
 $('cgHistoryDetail').textContent='#'+r.tableRoundId+' · '+stamp(r.createdAt)+' · '+t('Bet')+' '+money(stake)+' · '+t('Net')+' '+(bets.length?money(payout-stake):'—');
 document.querySelectorAll('[data-history-id]').forEach(b=>b.setAttribute('aria-pressed',String(b.dataset.historyId===String(cgHistorySelected))));
}
async function cgLoad(){cgReset();cgUpdate();await cgFetch();await cgHistory();}
function cgClock(){if(activeView!=='colorGameView')return;cgUpdate();if(cgState){const betting=cgBetting(),sec=Math.max(0,Math.ceil(((betting?cgState.bettingClosesAt:cgState.nextRoundAt)-cgNow())/1000));$('cgCountdown').textContent=String(sec).padStart(2,'0');$('cgPhase').textContent=t(betting?'BETTING OPEN':'CARDS OPEN');$('cgClock').classList.toggle('urgent',betting&&sec<=3);if(cgTick!==sec){cgTick=sec;if(betting&&sec<=3)cgCue('tick');}}
 if(Date.now()-cgLastFetch>900&&!cgSending&&!cgBusy)void cgFetch();}
document.querySelectorAll('[data-cg-side]').forEach(b=>b.onclick=()=>cgPlay(b.dataset.cgSide));document.querySelectorAll('[data-cg-chip]').forEach(b=>b.onclick=()=>{cgChip=+b.dataset.cgChip;cgCue('select');cgUpdate();});
$('cgLobby').onclick=()=>switchMode('LOBBY');$('cgClub').onclick=()=>switchMode('CLUB');$('cgRetry').onclick=()=>cgDrain();$('cgRulesButton').onclick=()=>$('cgRules').showModal();$('cgRulesClose').onclick=()=>$('cgRules').close();$('cgHistoryButton').onclick=()=>{cgHistoryPage=0;cgHistorySelected=null;cgHistoryRender();$('cgHistoryDialog').showModal();void cgHistory();};$('cgHistoryClose').onclick=()=>$('cgHistoryDialog').close();$('cgHistoryRefresh').onclick=()=>cgHistory();$('cgHistoryPrev').onclick=()=>{cgHistoryPage=Math.max(0,cgHistoryPage-1);cgHistoryRender();$('cgHistoryList').parentElement.scrollLeft=0;};$('cgHistoryNext').onclick=()=>{cgHistoryPage++;cgHistoryRender();$('cgHistoryList').parentElement.scrollLeft=0;};$('cgHistoryList').onclick=e=>{const b=e.target.closest('[data-history-id]');if(b){cgHistorySelected=b.dataset.historyId;cgHistoryDetail();}};$('cgSound').onclick=()=>{$('sound').click();cgUpdate();if(sound)cgCue('win');};
$('cgJackpotHelp').onclick=()=>$('cgJackpotDialog').showModal();$('cgJackpotClose').onclick=()=>$('cgJackpotDialog').close();
const cgOldHelp=$('help').onclick;$('help').onclick=()=>activeView==='colorGameView'?$('cgRules').showModal():cgOldHelp();
const cgOldLanguage=setLanguage;setLanguage=value=>{cgOldLanguage(value);cgHistoryRender();cgUpdate();if(cgState?.outcome&&cgShown===cgState.roundId&&!cgBusy){cgDice(cgState.outcome);cgResultText(cgState.outcome);}};
document.addEventListener('visibilitychange',()=>{if(document.hidden&&cgFinishReveal)cgFinishReveal();if(!document.hidden&&activeView==='colorGameView')void cgLoad();});setInterval(cgClock,200);$('cgResult').removeAttribute('data-i18n');cgDice();localize();

// Decorative flights only: balances and outcomes remain server-authoritative.
const cgReturnSeen=new Set();
function cgChipFlight(from,to,label='◉'){
 if(!from||!to||activeView!=='colorGameView'||document.hidden||matchMedia('(prefers-reduced-motion: reduce)').matches||document.body.dataset.cgQuality==='battery')return;
 const host=$('colorGameView');if(host.querySelectorAll('.cg-flying-chip').length>=12)return;
 const bounds=host.getBoundingClientRect(),rotated=document.body.classList.contains('cg-rotated');
 const point=el=>{const r=el.getBoundingClientRect(),x=r.left+r.width/2,y=r.top+r.height/2;return rotated?{x:y-bounds.top+host.scrollLeft,y:bounds.right-x+host.scrollTop}:{x:x-bounds.left+host.scrollLeft,y:y-bounds.top+host.scrollTop}};
 const a=point(from),b=point(to),coin=document.createElement('i');coin.className='cg-flying-chip';coin.textContent=label;coin.setAttribute('aria-hidden','true');coin.style.left=a.x+'px';coin.style.top=a.y+'px';host.append(coin);
 const animation=coin.animate([{transform:'translate(-50%,-50%) scale(.7)',opacity:0},{offset:.18,transform:'translate(-50%,-50%) scale(1)',opacity:1},{offset:.6,transform:'translate(calc(-50% + '+((b.x-a.x)*.6)+'px),calc(-50% + '+((b.y-a.y)*.6-25)+'px)) scale(1)'},{transform:'translate(calc(-50% + '+(b.x-a.x)+'px),calc(-50% + '+(b.y-a.y)+'px)) scale(.6)',opacity:0}],{duration:520,easing:'ease-out'});
 animation.finished.catch(()=>{}).finally(()=>coin.remove());
}
function cgReturnChips(outcome){
 const key=cgOwner+':'+cgState?.roundId;if(cgReturnSeen.has(key))return;
 cgReturnSeen.add(key);if(cgReturnSeen.size>128)cgReturnSeen.delete(cgReturnSeen.values().next().value);
 for(const side of new Set(outcome.dice))if(cgAmount(side)>0)cgChipFlight(document.querySelector('[data-cg-side="'+side+'"]'),$('cgBalance'));
}
