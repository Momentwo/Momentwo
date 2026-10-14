package cord.eoeo.momentwo.feature.photo.impl.presentation
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import cord.eoeo.momentwo.core.ui.CircleAsyncImage

/** Shared chrome and input. Rows, identity and storage remain supplied by each source. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun RecordsSheet(title: String, draft: TextFieldValue, onDraft: (TextFieldValue) -> Unit, onSave: () -> Unit,
    onDismiss: () -> Unit, pending: Boolean = false, canWrite: Boolean = true,
    sheet: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded=true), body: @Composable () -> Unit) {
    val ime=WindowInsets.isImeVisible
    ModalBottomSheet(onDismissRequest=onDismiss,sheetState=sheet,
        dragHandle={Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.fillMaxWidth()){BottomSheetDefaults.DragHandle();Text(title,fontWeight=FontWeight.SemiBold);Spacer(Modifier.height(8.dp))}},
        contentWindowInsets={WindowInsets.ime},modifier=Modifier.statusBarsPadding().imePadding()) {
        Column(Modifier.animateContentSize().fillMaxWidth().fillMaxHeight(if(ime)1f else 0.6f).navigationBarsPadding()) {
            Box(Modifier.weight(1f).fillMaxWidth()) { body() }
            TextField(value=draft,onValueChange=onDraft,placeholder={Text("$title 입력")},enabled=canWrite,
                trailingIcon={if(draft.text.isNotEmpty())Column(Modifier.padding(horizontal=8.dp)){IconButton(onClick=onSave,enabled=!pending,colors=IconButtonDefaults.iconButtonColors(containerColor=MaterialTheme.colorScheme.primary)){Icon(Icons.Default.ArrowUpward,"$title 작성",tint=MaterialTheme.colorScheme.onPrimary)}}},
                shape=RoundedCornerShape(16.dp),colors=TextFieldDefaults.colors(unfocusedIndicatorColor=Color.Transparent,focusedIndicatorColor=Color.Transparent),
                modifier=Modifier.fillMaxWidth().heightIn(min=TextFieldDefaults.MinHeight,max=TextFieldDefaults.MinHeight*2).padding(vertical=8.dp,horizontal=16.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun RecordRow(loader: ImageLoader, author: String, date: String, image: String, text: String,
    label: String, editable: Boolean, edit: () -> Unit, delete: () -> Unit, modifier: Modifier=Modifier) {
    var menu by rememberSaveable { mutableStateOf(false) }
    val focus=LocalFocusManager.current
    Row(modifier.combinedClickable(onClick={focus.clearFocus()},onLongClick={if(editable)menu=true}).padding(vertical=12.dp,horizontal=16.dp)) {
        if(image.isNotEmpty())CircleAsyncImage(image,"작성자 사진",loader,modifier=Modifier.padding(vertical=6.dp).height(38.dp))
        Column(Modifier.weight(1f).padding(start=8.dp)) {
            Row { if(author.isNotEmpty())Text(author,fontSize=14.sp,fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(end=8.dp));if(date.isNotEmpty())Text(date,fontSize=14.sp,color=Color.Gray,modifier=Modifier.padding(end=8.dp)) }
            Text(text,Modifier.padding(vertical=2.dp))
        }
        DropdownMenu(menu,{menu=false},Modifier.padding(horizontal=8.dp)) {
            DropdownMenuItem(text={Text("$label 수정하기")},onClick={menu=false;edit()},leadingIcon={Icon(Icons.Default.Edit,"$label 수정")})
            DropdownMenuItem(text={Text("$label 삭제하기")},onClick={menu=false;delete()},leadingIcon={Icon(Icons.Default.Clear,"$label 삭제")})
        }
    }
}
