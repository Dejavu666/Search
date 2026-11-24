package id.dejavu.search

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val PREFS = "dejavu_search_settings"
private const val CHROME_PACKAGE = "com.android.chrome"

enum class SafeMode(val label: String, val parameter: String?) {
    GOOGLE_DEFAULT("Google default", null),
    FILTER("Filter", "active"),
    BLUR("Blur", "moderate"),
    OFF("Off (requested)", "off")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DejavuSearchScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DejavuSearchScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, 0) }

    var query by remember { mutableStateOf("") }
    var safeMode by remember {
        mutableStateOf(
            runCatching { SafeMode.valueOf(prefs.getString("safe_mode", "OFF")!!) }
                .getOrDefault(SafeMode.OFF)
        )
    }
    var regionUs by remember { mutableStateOf(prefs.getBoolean("region_us", true)) }
    var languageEn by remember { mutableStateOf(prefs.getBoolean("language_en", true)) }
    var menuExpanded by remember { mutableStateOf(false) }

    fun saveSettings() {
        prefs.edit()
            .putString("safe_mode", safeMode.name)
            .putBoolean("region_us", regionUs)
            .putBoolean("language_en", languageEn)
            .apply()
    }

    fun makeSearchUrl(): String {
        val encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.toString())
        val params = mutableListOf("q=$encoded", "pws=0")
        if (languageEn) params += "hl=en"
        if (regionUs) params += "gl=us"
        safeMode.parameter?.let { params += "safe=$it" }
        return "https://www.google.com/search?${params.joinToString("&")}"
    }

    fun openInChrome(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage(CHROME_PACKAGE)
        }
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Google Chrome tidak ditemukan. Instal atau aktifkan Chrome.", Toast.LENGTH_LONG).show()
        }
    }

    MaterialTheme(colorScheme = darkColorScheme()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("DEJAVU Search", fontWeight = FontWeight.Bold)
                            Text("Google · Chrome", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text("Search", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Cari di Google") },
                    placeholder = { Text("Masukkan kata kunci…") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                Button(
                    onClick = {
                        if (query.isBlank()) {
                            Toast.makeText(context, "Masukkan kata kunci pencarian.", Toast.LENGTH_SHORT).show()
                        } else openInChrome(makeSearchUrl())
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cari dengan Chrome")
                }

                HorizontalDivider()
                Text("Search preferences", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

                PreferenceSwitch(
                    title = "Search language: English (US)",
                    subtitle = "Mengirim parameter hl=en.",
                    checked = languageEn,
                    onCheckedChange = { languageEn = it; saveSettings() }
                )
                PreferenceSwitch(
                    title = "Search region: United States",
                    subtitle = "Mengirim parameter gl=us.",
                    checked = regionUs,
                    onCheckedChange = { regionUs = it; saveSettings() }
                )

                ExposedDropdownMenuBox(
                    expanded = menuExpanded,
                    onExpandedChange = { menuExpanded = !menuExpanded }
                ) {
                    OutlinedTextField(
                        value = safeMode.label,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        label = { Text("SafeSearch preference") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        SafeMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.label) },
                                onClick = {
                                    safeMode = mode
                                    menuExpanded = false
                                    saveSettings()
                                }
                            )
                        }
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Status konfigurasi", fontWeight = FontWeight.Bold)
                        StatusLine("Bahasa", if (languageEn) "English (hl=en)" else "Default Google")
                        StatusLine("Region", if (regionUs) "United States (gl=us)" else "Default Google")
                        StatusLine("SafeSearch", safeMode.label)
                        HorizontalDivider()
                        Text(
                            "Transparansi: status di atas menunjukkan preferensi yang dikirim aplikasi, bukan konfirmasi bahwa Google menerapkannya. Google dapat mengabaikan atau mengunci SafeSearch karena kebijakan akun, perangkat, jaringan, atau wilayah. Aplikasi ini tidak dapat mendeteksi secara andal alasan penguncian dari halaman hasil pencarian.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        openInChrome("https://www.google.com/preferences?hl=en&gl=us")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Buka preferensi Google di Chrome")
                }

                Text(
                    "Privasi: aplikasi tidak meminta izin internet, tidak menyimpan riwayat pencarian, dan membuka pencarian langsung di Chrome. Chrome dan Google tetap menangani data sesuai pengaturan akun dan browser.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("DEJAVU Search 0.1.0 · Android 6.0+", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun PreferenceSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun StatusLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}
