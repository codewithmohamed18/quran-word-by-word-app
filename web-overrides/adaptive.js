// A dedicated 48px mode control stays accessible even when the title is truncated.
const modesButton=document.createElement('button');
modesButton.id='modes-button';modesButton.setAttribute('aria-label','Reading modes');modesButton.title='Reading modes';modesButton.textContent='▣';
modesButton.onclick=()=>screen('modes');document.querySelector('header').insertBefore(modesButton,$('bookmark'));
const nativeAppearance=()=>{if(window.Android&&Android.appearance)Android.appearance(document.body.classList.contains('dark'))};
new MutationObserver(nativeAppearance).observe(document.body,{attributes:true,attributeFilter:['class']});nativeAppearance();
