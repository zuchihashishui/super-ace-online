// The scene occupies only part of the viewport. Use a 1080px short-edge screen
// density target, not a stretched 1920x1080 canvas, and bound GPU allocations.
export function renderDensity(width,height,viewportWidth,viewportHeight,dpr,maxDimension=4096){
 const shortEdge=Math.max(1,Math.min(viewportWidth,viewportHeight));
 const wanted=Math.min(4,Math.max(1,Number.isFinite(dpr)?dpr:1,1080/shortEdge));
 return Math.min(wanted,Math.sqrt(3000000/Math.max(1,width*height)),maxDimension/Math.max(1,width,height));
}
