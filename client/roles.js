'use strict';
Object.assign(filipino,{'My players':'Mga manlalaro ko','Agents and players':'Mga agent at manlalaro','Club management':'Pamamahala ng club','My win / loss':'Aking panalo / talo','Player reports':'Mga ulat ng manlalaro','Creator can issue Club chips without an existing balance.':'Maaaring lumikha ang Creator ng chips sa Club kahit walang balanse.','Transfers use your Club chip balance and are limited to your own hierarchy.':'Gamit ang balanse ng chips mo sa Club, maaari lamang maglipat sa iyong mga nasasakupan.','Available Club chips':'Magagamit na chips sa Club'});
const transferHelp=document.createElement('p');transferHelp.className='muted';transferHelp.id='transferHelp';$('moveChipsForm').prepend(transferHelp);
const roleBase=applyRole;applyRole=()=>{
 roleBase();const role=player?.role;
 const accountTitle=role==='CREATOR'?'Club management':role==='SUPER_AGENT'?'Agents and players':'My players';
 const reportTitle=role==='PLAYER'?'My win / loss':'Player reports';
 for(const el of [document.querySelector('[data-view="accountsView"]'),$('accountsView').querySelector('h1')]){el.dataset.i18n=accountTitle;el.textContent=t(accountTitle)}
 for(const el of [document.querySelector('[data-view="reportsView"]'),$('reportsView').querySelector('h1')]){el.dataset.i18n=reportTitle;el.textContent=t(reportTitle)}
 transferHelp.textContent=role==='CREATOR'?t('Creator can issue Club chips without an existing balance.'):t('Available Club chips')+': '+fmt((player?.clubChipsCents||0)/100)+' · '+t('Transfers use your Club chip balance and are limited to your own hierarchy.');
};
const baseLanguage=setLanguage;setLanguage=value=>{baseLanguage(value);applyRole();modeUI()};
applyRole();
