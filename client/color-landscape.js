'use strict';
// Rotate only the game surface when a phone cannot lock its physical orientation.
Object.assign(filipino,{'Games':'Mga laro','Full screen':'Buong screen','Turn your phone sideways':'Ihiga ang iyong telepono'});
let cgOwnFullscreen=false,cgOwnOrientation=false;
function cgScreenLayout(){
 const phone=matchMedia('(pointer: coarse)').matches&&Math.min(innerWidth,innerHeight)<=600&&Math.max(innerWidth,innerHeight)<=1200;
 const on=activeView==='colorGameView'&&phone,rotated=on&&innerHeight>innerWidth;
 document.body.classList.toggle('cg-wide',on);document.body.classList.toggle('cg-rotated',rotated);
 document.documentElement.style.setProperty('--cg-screen-w',(rotated?innerHeight:innerWidth)+'px');document.documentElement.style.setProperty('--cg-screen-h',(rotated?innerWidth:innerHeight)+'px');
 if(!on&&cgOwnOrientation){try{screen.orientation.unlock()}catch{}cgOwnOrientation=false;}
 if(!on&&cgOwnFullscreen){cgOwnFullscreen=false;if(document.fullscreenElement)void document.exitFullscreen().catch(()=>{});}
}
const cgScreenBaseShow=showView;showView=async function(id){const result=cgScreenBaseShow(id);cgScreenLayout();await result;cgScreenLayout();};
$('cgExit').onclick=()=>showView('gameView');
$('cgFullscreen').onclick=async()=>{
 try{if(!document.fullscreenElement&&document.documentElement.requestFullscreen){await document.documentElement.requestFullscreen();cgOwnFullscreen=true;}if(activeView==='colorGameView'&&screen.orientation?.lock){await screen.orientation.lock('landscape');cgOwnOrientation=true;}}catch{ /* CSS rotation remains available when the browser refuses native locking. */ }finally{cgScreenLayout();}
};
addEventListener('resize',cgScreenLayout);screen.orientation?.addEventListener('change',cgScreenLayout);window.visualViewport?.addEventListener('resize',cgScreenLayout);document.addEventListener('fullscreenchange',()=>{if(!document.fullscreenElement){cgOwnFullscreen=false;if(cgOwnOrientation){try{screen.orientation.unlock()}catch{}cgOwnOrientation=false;}}cgScreenLayout();});
cgScreenLayout();
