'use strict';
Object.assign(filipino,{
 'Your chips':'Iyong mga chip','Others':'Iba pang manlalaro','bettors':'manlalarong tumaya',
 'chips placed':'chip na naitaya','Waiting for bets':'Naghihintay ng taya','Updating…':'Ina-update…',
 'Totals unavailable':'Hindi makuha ang kabuuan','Bettors this round':'Mga tumaya sa larong ito',
 'Confirmed bets only · Other players exclude you':'Tinanggap na taya lamang · Hindi ka kasama sa ibang manlalaro',
 'Stack preview; count includes every chip':'Ipinapakitang stack; kasama sa bilang ang lahat ng chip'
});
let dtCrowdState=null,dtCrowdRequest=false,dtCrowdAt=0,dtCrowdKey='',dtStackKey='',dtCrowdFailed=false;
const dtBettorBadge=document.createElement('span');dtBettorBadge.id='dtBettorCount';dtBettorBadge.className='dt-bettor-count';document.querySelector('.dt-toolbar').after(dtBettorBadge);
const dtChipCount=document.createElement('small');dtChipCount.id='dtOwnChipCount';$('dtTotal').after(dtChipCount);
// Keep selection and betting areas adjacent; an overlay would hide totals on narrow phones.
document.querySelector('.dt-bets').before(document.querySelector('.dt-chip-console'));
const dtCrowdNote=document.createElement('p');dtCrowdNote.className='dt-crowd-note';dtCrowdNote.dataset.i18n='Confirmed bets only · Other players exclude you';dtCrowdNote.textContent='Confirmed bets only · Other players exclude you';document.querySelector('.dt-bets').after(dtCrowdNote);
for(const button of document.querySelectorAll('[data-dt-side]')){
 const stack=document.createElement('span');stack.className='dt-stack-area';stack.setAttribute('aria-label',t('Your chips'));button.querySelector('.dt-side-total').before(stack);
 const count=document.createElement('small');count.className='dt-own-chip-count';button.querySelector('.dt-side-total').after(count);
 const other=document.createElement('span');other.className='dt-other-bets';other.innerHTML='<span class="dt-other-label"></span><strong class="dt-other-value">—</strong><small class="dt-other-players"></small>';button.append(other);
}
function dtCrowdIdentity(){return dtKey()+':'+gameMode+':'+(dtTableState?.roundId??'')}
function dtDenomination(cents){return String(cents/100)}
function dtRenderStacks(){
 const identity=dtCrowdIdentity();if(identity!==dtCrowdKey){dtCrowdKey=identity;dtCrowdState=null;dtCrowdAt=0;dtCrowdFailed=false;dtStackKey=''}
 const rows=dtBets.filter(b=>b.tableRoundId===dtTableState?.roundId),signature=identity+':'+document.documentElement.lang+':'+rows.map(b=>b.requestId).join(',');
 if(signature!==dtStackKey){dtStackKey=signature;
  for(const button of document.querySelectorAll('[data-dt-side]')){
   const bets=rows.filter(b=>b.side===button.dataset.dtSide),denominations=new Map();for(const bet of bets)denominations.set(bet.betCents,(denominations.get(bet.betCents)||0)+1);
   const area=button.querySelector('.dt-stack-area');area.replaceChildren();area.setAttribute('aria-label',t('Your chips'));
   for(const [cents,count] of [...denominations].sort((a,b)=>a[0]-b[0])){
    const group=document.createElement('span');group.className='dt-chip-stack';group.dataset.cents=String(cents);group.dataset.count=String(count);group.setAttribute('aria-label',count+' × '+dtDenomination(cents));group.title=t('Stack preview; count includes every chip');
    // Bound the decorative layers; the exact count and monetary total are never capped.
    const visible=Math.min(count,6);group.style.setProperty('--layers',String(visible));
    for(let i=0;i<visible;i++){const coin=document.createElement('span');coin.className='dt-stacked-chip';coin.dataset.denom=dtDenomination(cents);coin.textContent=dtDenomination(cents);coin.style.setProperty('--layer',String(i));coin.setAttribute('aria-hidden','true');group.append(coin)}
    const label=document.createElement('span');label.className='dt-stack-count';label.textContent='×'+count;label.setAttribute('aria-hidden','true');group.append(label);area.append(group);
   }
   if(!bets.length){const empty=document.createElement('span');empty.className='dt-empty-stack';empty.textContent='○';empty.setAttribute('aria-hidden','true');area.append(empty)}
   button.querySelector('.dt-own-chip-count').textContent=bets.length+' '+t('chips placed');
  }
  $('dtOwnChipCount').textContent=rows.length+' '+t('chips placed');
 }
 const fresh=dtCrowdState&&Date.now()-dtCrowdAt<4000;
 for(const button of document.querySelectorAll('[data-dt-side]')){
  const item=dtCrowdState?.sides.find(s=>s.side===button.dataset.dtSide);button.querySelector('.dt-other-label').textContent=t('Others');button.querySelector('.dt-other-value').textContent=fresh&&item?money(item.othersCents):'—';button.querySelector('.dt-other-players').textContent=fresh&&item?item.otherBettors+' '+t('bettors'):t(dtCrowdFailed?'Totals unavailable':'Updating…');
 }
 $('dtBettorCount').textContent=fresh?t('Bettors this round')+': '+dtCrowdState.bettors:t(dtCrowdFailed?'Totals unavailable':'Updating…');
}
async function dtFetchCrowd(){
 if(dtCrowdRequest||!dtTableState||activeView!=='dragonTigerView'||document.hidden)return;
 dtCrowdRequest=true;const key=dtCrowdIdentity(),mode=gameMode,id=dtTableState.roundId;
 try{const snapshot=await api('dragon-tiger/crowd?mode='+mode+'&roundId='+id);if(key===dtCrowdIdentity()){dtCrowdState=snapshot;dtCrowdAt=Date.now();dtCrowdFailed=false}}
 catch{if(key===dtCrowdIdentity()){dtCrowdFailed=true;dtCrowdState=null}}
 finally{dtCrowdRequest=false;dtRenderStacks()}
}
const dtStackBaseUpdate=dtUpdate;dtUpdate=()=>{dtStackBaseUpdate();dtRenderStacks()};
const dtStackBaseLoad=dtLoad;dtLoad=async()=>{await dtStackBaseLoad();await dtFetchCrowd()};
setInterval(()=>{if(activeView==='dragonTigerView'){dtRenderStacks();void dtFetchCrowd()}},800);
document.addEventListener('visibilitychange',()=>{if(!document.hidden&&activeView==='dragonTigerView')void dtFetchCrowd()});
dtRenderStacks();localize();
