package com.icontree.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.icontree.animate.Icon
import com.icontree.animate.IconMode
import com.icontree.animate.IconTree
import com.icontree.animate.iconIds
import com.icontree.animate.rememberIconTrigger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface(Modifier.fillMaxSize()) { Gallery() } } }
    }
}

private val palette = listOf(
    "Current" to Color.Unspecified,
    "Red" to Color(0xFFE5484D),
    "Blue" to Color(0xFF2477D4),
    "Green" to Color(0xFF30A46C),
)

@Composable
private fun Gallery() {
    var query by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(IconMode.Loop) }
    var color by remember { mutableStateOf(Color.Unspecified) }
    var size by remember { mutableFloatStateOf(40f) }
    var clicks by remember { mutableStateOf(0) }
    val trigger = rememberIconTrigger()

    val ids = remember(query) { if (query.isBlank()) iconIds else IconTree.search(query) }

    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp).padding(top = 40.dp)) {
        Text("icon-tree-animate · ${iconIds.size} icons", fontSize = 18.sp)
        OutlinedTextField(
            value = query, onValueChange = { query = it }, singleLine = true,
            label = { Text("Search (e.g. \"arrow right\" or \"heart,star\")") },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconMode.entries.forEach {
                FilterChip(selected = mode == it, onClick = { mode = it }, label = { Text(it.name) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            palette.forEach { (name, c) ->
                FilterChip(selected = color == c, onClick = { color = c }, label = { Text(name) })
            }
        }
        Slider(value = size, onValueChange = { size = it }, valueRange = 16f..96f)

        // bindId equivalent: a normal button that starts an onClick icon.
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { trigger.play() }) { Text("Like") }
            Icon(id = "heart", size = 32.dp, color = Color(0xFFE5484D), mode = IconMode.OnClick,
                trigger = trigger, onClick = { clicks++ })
            Text("triggered $clicks×")
        }

        Text("${ids.size} results", modifier = Modifier.padding(vertical = 4.dp))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(88.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(ids, key = { it }) { id ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(id = id, size = size.dp, color = color, mode = mode)
                    Text(id, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
