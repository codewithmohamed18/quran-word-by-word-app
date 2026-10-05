package com.codewithmohamed.quranwordbyword

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.activity.compose.LocalActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

private val Gold=Color(0xFFA78136)
private val Light=lightColorScheme(primary=Color(0xFF745721),onPrimary=Color.White,
    secondary=Color(0xFF2F5A50),background=Color(0xFFFAF7EF),surface=Color(0xFFFAF7EF),
    surfaceContainer=Color(0xFFF0E9DA),onSurface=Color(0xFF272B26))
private val Dark=darkColorScheme(primary=Color(0xFFE1C07C),onPrimary=Color(0xFF3F3016),
    secondary=Color(0xFFADCFC0),background=Color(0xFF171C19),surface=Color(0xFF171C19),
    surfaceContainer=Color(0xFF242C26),onSurface=Color(0xFFECE9DF))

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun QuranApp(model: ReaderViewModel) {
    val state by model.state.collectAsStateWithLifecycle()
    val systemDark=isSystemInDarkTheme()
    val dark=when(state.preferences.theme) { "dark"->true; "light"->false; else->systemDark }
    MaterialTheme(colorScheme=if(dark) Dark else Light) {
        val drawer=rememberDrawerState(DrawerValue.Closed)
        val scope=rememberCoroutineScope()
        var screen by rememberSaveable { mutableStateOf("reader") }
        var jump by rememberSaveable { mutableStateOf(false) }
        var more by remember { mutableStateOf(false) }
        var hideControls by rememberSaveable { mutableStateOf(true) }
        val view=LocalView.current
        val activity=LocalActivity.current
        SideEffect {
            activity?.window?.let { window ->
                WindowCompat.getInsetsController(window,view).apply {
                    isAppearanceLightStatusBars=!dark
                    isAppearanceLightNavigationBars=!dark
                }
            }
        }
        val immersive=screen=="reader" && hideControls && !jump && drawer.isClosed
        DisposableEffect(activity,view,immersive) {
            val controller=activity?.window?.let { WindowCompat.getInsetsController(it,view) }
            controller?.systemBarsBehavior=WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if(immersive) controller?.hide(WindowInsetsCompat.Type.systemBars())
            else controller?.show(WindowInsetsCompat.Type.systemBars())
            onDispose { controller?.show(WindowInsetsCompat.Type.systemBars()) }
        }
        DisposableEffect(view,state.preferences.keepAwake,screen) {
            view.keepScreenOn=state.preferences.keepAwake && screen=="reader"
            onDispose { view.keepScreenOn=false }
        }
        fun openScreen(target: String) { screen=target; scope.launch { drawer.close() } }
        fun navigate(page: Int) { model.goTo(page); screen="reader" }
        BackHandler(drawer.isOpen || screen!="reader" || hideControls) {
            when { drawer.isOpen->scope.launch { drawer.close() }; screen!="reader"->screen="reader"; else->hideControls=false }
        }
        ModalNavigationDrawer(drawerState=drawer,gesturesEnabled=drawer.isOpen,drawerContent={
            ModalDrawerSheet(modifier=Modifier.widthIn(max=340.dp)) {
                LazyColumn(modifier=Modifier.fillMaxSize()) {
                    item {
                        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer).padding(24.dp)) {
                            Text("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",fontSize=24.sp,color=MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(20.dp))
                            Text("Qur’an Word by Word",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.SemiBold)
                            Text("Arabic & English · 30 Juz",style=MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(12.dp))
                            Text("Complete Qur’an · always offline",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    item { NavigationDrawerItem(label={ Text("Continue Reading — Page ${state.preferences.lastPage}") },
                        selected=false,icon={ Icon(Icons.Default.PlayArrow,null) },modifier=Modifier.padding(horizontal=12.dp),
                        onClick={ navigate(state.preferences.lastPage); scope.launch { drawer.close() } }) }
                    item { DrawerEntry("Juz (Para)",Icons.Default.MenuBook,screen=="juz") { openScreen("juz") } }
                    item { DrawerEntry("Surah",Icons.Default.FormatListNumbered,screen=="surah") { openScreen("surah") } }
                    item { DrawerEntry("Bookmarks",Icons.Default.Bookmarks,screen=="bookmarks") { openScreen("bookmarks") } }
                    item { DrawerEntry("Go to Page",Icons.Default.NearMe,false) { jump=true; scope.launch { drawer.close() } } }
                    item { DrawerEntry("Settings",Icons.Default.Settings,screen=="settings") { openScreen("settings") } }
                    item { DrawerEntry("About",Icons.Default.Info,screen=="about") { openScreen("about") } }
                    item { Text("READ · REFLECT · RETURN",Modifier.padding(28.dp),style=MaterialTheme.typography.labelSmall,color=Gold) }
                }
            }
        }) {
            Scaffold(contentWindowInsets=if(immersive) WindowInsets(0,0,0,0) else ScaffoldDefaults.contentWindowInsets,
                containerColor=MaterialTheme.colorScheme.background,topBar={
                if(!hideControls || screen!="reader") TopAppBar(title={
                    Text(when(screen) { "juz"->"Juz (Para)"; "surah"->"Surahs"; "bookmarks"->"Bookmarks";
                        "settings"->"Settings"; "about"->"About"; else->"Qur’an Word by Word" },
                        style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold)
                },navigationIcon={
                    IconButton(onClick={ if(screen=="reader") scope.launch { drawer.open() } else screen="reader" }) {
                        Icon(if(screen=="reader") Icons.Default.Menu else Icons.AutoMirrored.Filled.ArrowBack,
                            if(screen=="reader") "Open navigation menu" else "Back to reader")
                    }
                },actions={
                    if(screen=="reader") {
                        IconButton(onClick={ model.bookmark() },enabled=state.displayed) {
                            Icon(if(state.page in state.preferences.bookmarks) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                if(state.page in state.preferences.bookmarks) "Remove bookmark" else "Bookmark current page")
                        }
                        Box {
                            IconButton(onClick={ more=true }) { Icon(Icons.Default.MoreVert,"Reading options") }
                            DropdownMenu(expanded=more,onDismissRequest={more=false}) {
                                DropdownMenuItem(text={Text("Fit page")},onClick={model.fit();more=false})
                                DropdownMenuItem(text={Text("Go to page")},onClick={jump=true;more=false})
                                DropdownMenuItem(text={Text("Hide controls")},onClick={hideControls=true;more=false})
                                DropdownMenuItem(text={Text("Settings")},onClick={screen="settings";more=false})
                            }
                        }
                    }
                })
            },bottomBar={
                if(screen=="reader" && !hideControls) Surface(tonalElevation=2.dp) {
                    Row(Modifier.fillMaxWidth().navigationBarsPadding().heightIn(min=56.dp),
                        horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
                        IconButton(onClick={navigate(state.page-1)},enabled=state.ready && state.page>1) {
                            Icon(Icons.Default.NavigateBefore,"Previous page")
                        }
                        TextButton(onClick={jump=true},enabled=state.ready) { Text("Page ${state.page} / 960",fontWeight=FontWeight.Medium) }
                        IconButton(onClick={navigate(state.page+1)},enabled=state.ready && state.page<960) {
                            Icon(Icons.Default.NavigateNext,"Next page")
                        }
                    }
                }
            }) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when(screen) {
                        "reader" -> ReaderSurface(state,model,fullScreen=hideControls,onTap={ hideControls=!hideControls })
                        "juz" -> Chapters(state.config?.juz.orEmpty(),false,state.page,onSelect={navigate(it)})
                        "surah" -> Chapters(state.config?.surahs.orEmpty(),true,state.page,onSelect={navigate(it)})
                        "bookmarks" -> BookmarkScreen(state.preferences.bookmarks,state.config?.juz.orEmpty(),onSelect={navigate(it)},onRemove={page ->
                            // Toggle the requested bookmark independently of the current reading page.
                            model.removeBookmark(page)
                        })
                        "settings" -> SettingsScreen(state.preferences,model)
                        "about" -> AboutScreen()
                    }
                }
            }
        }
        if(jump) PageDialog(state.page,onDismiss={jump=false},onGo={navigate(it);jump=false})
    }
}
@Composable private fun DrawerEntry(label: String,icon: androidx.compose.ui.graphics.vector.ImageVector,selected: Boolean,action: ()->Unit) {
    NavigationDrawerItem(label={Text(label)},icon={Icon(icon,null)},selected=selected,onClick=action,
        modifier=Modifier.padding(horizontal=12.dp))
}
@Composable private fun ReaderSurface(state: ReaderState,model: ReaderViewModel,fullScreen: Boolean,onTap: ()->Unit) {
    val background=Color.White.toArgb()
    Box(Modifier.fillMaxSize().testTag("pdf-reader").semantics { stateDescription=if(fullScreen) "Full screen" else "Reader controls shown" }) {
        if(state.ready) AndroidView(factory={ context -> PdfPageView(context).apply {
            onDisplayed=model::displayed; onFailure=model::failure; onSwipe={model.goTo(model.state.value.page+it)}
            this.onTap=onTap
        } },modifier=Modifier.fillMaxSize().semantics { contentDescription="Qur’an page ${state.page}. Pinch or double tap to zoom. Swipe at fit size to change pages." },
            onReset=null,onRelease={it.dispose()},update={ view ->
                view.setBackgroundColor(background); view.onTap=onTap; view.showPage(state.page,model.engine,state.fitRequest)
            })
        if((!state.ready || !state.displayed) && state.error==null) Column(Modifier.align(Alignment.Center),horizontalAlignment=Alignment.CenterHorizontally) {
            CircularProgressIndicator(); Spacer(Modifier.height(16.dp)); Text(if(state.ready) "Loading page ${state.page}…" else "Opening your Qur’an…")
        }
        state.error?.let { error -> ElevatedCard(Modifier.align(Alignment.Center).padding(24.dp)) {
            Column(Modifier.padding(24.dp)) { Text(error); if(state.ready) TextButton(onClick={ model.goTo(state.page+1) }) { Text("Try next page") } }
        } }
    }
}
@Composable private fun Chapters(chapters: List<Chapter>,surahs: Boolean,page: Int,onSelect: (Int)->Unit) {
    LazyColumn(Modifier.fillMaxSize().testTag("chapter-list"),contentPadding=PaddingValues(vertical=8.dp)) {
        item { Text(if(surahs) "114 Surahs · Qur’anic order" else "30 Juz · choose where to begin",Modifier.padding(20.dp),
            style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.secondary) }
        items(chapters,key={it.number}) { chapter ->
            ListItem(headlineContent={ Text(chapter.name,fontWeight=FontWeight.Medium) },
                supportingContent={Text("Page ${chapter.page}")},
                leadingContent={ Surface(shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surfaceContainer) {
                    Box(Modifier.size(44.dp),contentAlignment=Alignment.Center) { Text(chapter.number.toString(),color=MaterialTheme.colorScheme.primary) }
                } },trailingContent={ if(chapter.arabic.isNotBlank()) Text(chapter.arabic,fontSize=22.sp,color=MaterialTheme.colorScheme.primary) },
                modifier=Modifier.fillMaxWidth().clickable {onSelect(chapter.page)}.heightIn(min=76.dp))
            HorizontalDivider(Modifier.padding(horizontal=20.dp),color=MaterialTheme.colorScheme.outlineVariant.copy(alpha=.4f))
        }
    }
}
@Composable private fun BookmarkScreen(saved: Set<Int>,juz: List<Chapter>,onSelect:(Int)->Unit,onRemove:(Int)->Unit) {
    if(saved.isEmpty()) Box(Modifier.fillMaxSize().padding(32.dp),contentAlignment=Alignment.Center) {
        Column(horizontalAlignment=Alignment.CenterHorizontally) {
            Icon(Icons.Default.BookmarkBorder,null,Modifier.size(48.dp),tint=MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp)); Text("Your saved pages",style=MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp)); Text("Tap the bookmark icon while reading to save a page.")
        }
    } else LazyColumn(Modifier.fillMaxSize()) {
        items(saved.sorted(),key={it}) { page ->
            ListItem(headlineContent={ Text("Page $page") },supportingContent={Text("Juz ${juz.lastOrNull { it.page <= page }?.number ?: 1}")},
                leadingContent={Icon(Icons.Default.Bookmark,null,tint=MaterialTheme.colorScheme.primary)},
                trailingContent={IconButton(onClick={onRemove(page)}) {Icon(Icons.Default.DeleteOutline,"Remove bookmark for page $page")}},
                modifier=Modifier.clickable {onSelect(page)}.heightIn(min=76.dp))
        }
    }
}
@Composable private fun SettingsScreen(prefs: ReadingPreferences,model: ReaderViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("Appearance",style=MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        listOf("system" to "Use device theme","light" to "Warm light","dark" to "Dark mode").forEach { (key,name) ->
            Row(Modifier.fillMaxWidth().clickable{model.theme(key)}.heightIn(min=56.dp),verticalAlignment=Alignment.CenterVertically) {
                RadioButton(selected=prefs.theme==key,onClick={model.theme(key)}); Text(name)
            }
        }
        Spacer(Modifier.height(24.dp)); HorizontalDivider(); Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("Keep screen awake",style=MaterialTheme.typography.titleMedium); Text("Only while the reader is open",style=MaterialTheme.typography.bodySmall) }
            Switch(checked=prefs.keepAwake,onCheckedChange=model::keepAwake,modifier=Modifier.semantics{contentDescription="Keep screen awake while reading"})
        }
        Spacer(Modifier.height(24.dp)); Text("Reading",style=MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp)); Text("Pinch or double tap to zoom. Drag to pan when zoomed in. Swipe left or right at fit size to change pages. Pages fit the screen width. Tap once to show or hide controls. Fit page resets zoom.")
        Spacer(Modifier.height(16.dp)); Text("Dark mode changes the app controls and background. The original Qur’an page colours remain unchanged.",style=MaterialTheme.typography.bodySmall)
    }
}
@Composable private fun AboutScreen() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Text("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",fontSize=28.sp,color=MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(24.dp)); Text("Qur’an Word by Word",style=MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp)); Text("Arabic–English · 960 pages · 30 Juz · 114 Surahs")
        Spacer(Modifier.height(24.dp)); Text("The complete Qur’an is included on your phone. Reading, bookmarks, progress and settings work offline. No account or advertisements.")
        Spacer(Modifier.height(16.dp)); Text("Source edition: haameem7.wordpress.com, Arabic–English word-by-word Qur’an. Original PDF page content is preserved.")
        Spacer(Modifier.height(16.dp)); Text("An original cream-and-gold interface. Version 3.1",style=MaterialTheme.typography.labelLarge)
    }
}
@Composable private fun PageDialog(page: Int,onDismiss:()->Unit,onGo:(Int)->Unit) {
    var value by rememberSaveable {mutableStateOf(page.toString())}; var error by remember {mutableStateOf(false)}
    AlertDialog(onDismissRequest=onDismiss,title={Text("Go to page")},text={
        OutlinedTextField(value=value,onValueChange={value=it;error=false},label={Text("Page 1–960")},singleLine=true,
            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),isError=error,
            supportingText={ if(error) Text("Enter a whole page number from 1 to 960") })
    },confirmButton={TextButton(onClick={val n=value.toIntOrNull();if(n!=null && n in 1..960) onGo(n) else error=true}) {Text("Go")}},
        dismissButton={TextButton(onClick=onDismiss) {Text("Cancel")}})
}
