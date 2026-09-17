'use strict';
Object.assign(filipino,{
 'One-card duel':'Laban ng tig-isang baraha','One card per side · Higher card wins':'Tig-isang baraha · Mas mataas ang panalo',
 'A low · K high':'A pinakamababa · K pinakamataas','Higher card wins · A low, K high':'Mas mataas ang panalo · A mababa, K mataas',
 'One card per side · Virtual chips only':'Tig-isang baraha · Virtual chips lamang',
 'Previous two-card rules':'Dating tuntunin: dalawang baraha'
});
// Keep the live controls as HTML: the illustration is decorative, not a screenshot UI.
const jadeConsole=document.querySelector('.dt-chip-console');
$('dtTable').querySelector('.dt-bets').after(jadeConsole);
$('dtResult').after($('dtRoad'));
const jadeFinal=dtShowFinal;
dtShowFinal=row=>{jadeFinal(row);if(row.outcome.dragon.length===1){for(const side of ['Dragon','Tiger']){const rank=row.outcome[side.toLowerCase()][0].rank;$('dt'+side+'Score').textContent=({1:'A',11:'J',12:'Q',13:'K'})[rank]||rank;}}};
const jadeInspect=dtInspectGame;
dtInspectGame=row=>{jadeInspect(row);if(row.outcome.dragon.length>1)$('dtInspectRound').textContent+=' · '+t('Previous two-card rules');};
localize();dtUpdate();
