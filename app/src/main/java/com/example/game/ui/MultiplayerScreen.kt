package com.example.game.ui

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.audio.SoundManager
import com.example.game.entities.GameMode
import com.example.game.persistence.WorldMetadata
import com.example.game.ui.options.JavaOptionButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Multiplayer Server Model representing an entry in the Minecraft Java Server List.
 */
data class ServerEntry(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val motdLine1: String = "A Minecraft Server",
    val motdLine2: String = "Minecraft 26.2 Compatible",
    val ping: Int = 18,
    val playersOnline: Int = 0,
    val maxPlayers: Int = 20,
    val version: String = "1.8 - 26.2",
    val isFreeServer: Boolean = false,
    val iconEmoji: String = "🌐",
    val resourcePackMode: String = "Prompt" // "Prompt", "Enabled", "Disabled"
)

/**
 * Local persistent storage for Multiplayer Servers.
 * Default is an empty/blank server list as requested by the user.
 */
object ServerStorage {
    private const val PREFS_NAME = "minecraft_servers_prefs"
    private const val KEY_SERVERS = "saved_servers_list"

    fun loadServers(context: Context): List<ServerEntry> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_SERVERS, null) ?: return emptyList() // Blank by default!

        return try {
            val array = JSONArray(jsonString)
            val list = mutableListOf<ServerEntry>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ServerEntry(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        name = obj.optString("name", "Minecraft Server"),
                        address = obj.optString("address", "localhost:25565"),
                        motdLine1 = obj.optString("motdLine1", "A Minecraft Server"),
                        motdLine2 = obj.optString("motdLine2", "Minecraft 26.2"),
                        ping = obj.optInt("ping", 18),
                        playersOnline = obj.optInt("playersOnline", 0),
                        maxPlayers = obj.optInt("maxPlayers", 20),
                        version = obj.optString("version", "1.8 - 26.2"),
                        isFreeServer = obj.optBoolean("isFreeServer", false),
                        iconEmoji = obj.optString("iconEmoji", "🌐"),
                        resourcePackMode = obj.optString("resourcePackMode", "Prompt")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveServers(context: Context, servers: List<ServerEntry>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        servers.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("address", s.address)
                put("motdLine1", s.motdLine1)
                put("motdLine2", s.motdLine2)
                put("ping", s.ping)
                put("playersOnline", s.playersOnline)
                put("maxPlayers", s.maxPlayers)
                put("version", s.version)
                put("isFreeServer", s.isFreeServer)
                put("iconEmoji", s.iconEmoji)
                put("resourcePackMode", s.resourcePackMode)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_SERVERS, array.toString()).apply()
    }
}

/**
 * Authentic Minecraft Java Edition "Play Multiplayer" Screen matching the screenshot:
 * - Top title: "Play Multiplayer"
 * - Server list: Starts blank as requested
 * - Bottom Action Buttons:
 *   Row 1: [Join Server] [Direct Connection] [Add Server] [Create Free Server]
 *   Row 2: [Edit] [Delete] [Refresh] [Back]
 */
@Composable
fun MultiplayerScreen(
    soundManager: SoundManager,
    onJoinServer: (WorldMetadata) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Server list starts blank (loaded from storage; blank initially)
    val serverList = remember {
        mutableStateListOf<ServerEntry>().apply {
            addAll(ServerStorage.loadServers(context))
        }
    }

    var selectedServerId by remember { mutableStateOf<String?>(null) }
    val selectedServer = serverList.find { it.id == selectedServerId }

    // Dialog and connecting states
    var showAddServerDialog by remember { mutableStateOf(false) }
    var showEditServerDialog by remember { mutableStateOf(false) }
    var showDirectConnectDialog by remember { mutableStateOf(false) }
    var showCreateFreeServerDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var isConnecting by remember { mutableStateOf(false) }
    var connectingStatusText by remember { mutableStateOf("Connecting to the server...") }
    var isRefreshing by remember { mutableStateOf(false) }

    fun triggerJoin(server: ServerEntry) {
        soundManager.playClick()
        isConnecting = true
        connectingStatusText = "Connecting to ${server.address}..."
        coroutineScope.launch {
            delay(500)
            connectingStatusText = "Logging in..."
            delay(500)
            connectingStatusText = "Loading terrain..."
            delay(600)
            isConnecting = false
            // Create multiplayer world session
            val multiplayerWorld = WorldMetadata(
                id = "server_${server.id}",
                name = "[Server] ${server.name}",
                seed = server.address.hashCode().toLong(),
                gameMode = GameMode.SURVIVAL,
                isFlat = false,
                lastPlayed = System.currentTimeMillis(),
                version = "26.2"
            )
            onJoinServer(multiplayerWorld)
        }
    }

    fun triggerDirectJoin(address: String) {
        soundManager.playClick()
        isConnecting = true
        connectingStatusText = "Connecting to $address..."
        coroutineScope.launch {
            delay(500)
            connectingStatusText = "Logging in..."
            delay(500)
            connectingStatusText = "Loading terrain..."
            delay(600)
            isConnecting = false
            val directWorld = WorldMetadata(
                id = "direct_${System.currentTimeMillis()}",
                name = "[Server] $address",
                seed = address.hashCode().toLong(),
                gameMode = GameMode.SURVIVAL,
                isFlat = false,
                lastPlayed = System.currentTimeMillis(),
                version = "26.2"
            )
            onJoinServer(directWorld)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE111111)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: "Play Multiplayer" with subtle drop shadow
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Shadow
                Text(
                    text = "Play Multiplayer",
                    color = Color(0xFF3F3F3F),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(top = 1.dp, start = 1.dp)
                )
                // Main
                Text(
                    text = "Play Multiplayer",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Center Server List Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(0.96f)
                    .background(Color(0xFF141414))
                    .border(1.5.dp, Color(0xFF3A3A3A), RoundedCornerShape(2.dp))
                    .clip(RoundedCornerShape(2.dp))
            ) {
                if (serverList.isEmpty()) {
                    // Blank state as requested by user
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Scanning for games on your local network...",
                            color = Color(0xFFAAAAAA),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "No multiplayer servers added yet.",
                            color = Color(0xFF777777),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            JavaOptionButton(
                                text = "+ Create Free Server",
                                height = 30.dp,
                                textColor = Color(0xFF81C784),
                                onClick = {
                                    soundManager.playClick()
                                    showCreateFreeServerDialog = true
                                }
                            )

                            JavaOptionButton(
                                text = "+ Add Server",
                                height = 30.dp,
                                textColor = Color.White,
                                onClick = {
                                    soundManager.playClick()
                                    showAddServerDialog = true
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(serverList, key = { it.id }) { server ->
                            val isSelected = server.id == selectedServerId

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isSelected) Color(0xFF242424) else Color(0x77161616))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color(0xFF333333)
                                    )
                                    .clickable {
                                        if (isSelected) {
                                            triggerJoin(server)
                                        } else {
                                            soundManager.playClick()
                                            selectedServerId = server.id
                                        }
                                    }
                                    .padding(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // 52x52 Server Icon / Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                if (server.isFreeServer) {
                                                    Brush.verticalGradient(
                                                        listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF43A047))
                                                    )
                                                } else {
                                                    Brush.verticalGradient(
                                                        listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF3949AB))
                                                    )
                                                }
                                            )
                                            .border(1.dp, Color(0xFF555555), RoundedCornerShape(2.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = server.iconEmoji,
                                            fontSize = 22.sp
                                        )

                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color(0x44000000)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Join",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Server Details Column
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = server.name,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            if (server.isFreeServer) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color(0xFF2E7D32), RoundedCornerShape(2.dp))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "FREE 24/7",
                                                        color = Color(0xFFA5D6A7),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }
                                        }

                                        // MOTD Line 1
                                        Text(
                                            text = server.motdLine1,
                                            color = if (server.isFreeServer) Color(0xFF81C784) else Color(0xFFFFF176),
                                            fontSize = 11.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        // MOTD Line 2 / Subtitle
                                        Text(
                                            text = server.motdLine2,
                                            color = Color(0xFFAAAAAA),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Right Side: Players & Ping Signal Bars
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Text(
                                                text = "${server.playersOnline}/${server.maxPlayers}",
                                                color = Color(0xFFAAAAAA),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )

                                            // Ping bars (5 green vertical signal bars)
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                                                verticalAlignment = Alignment.Bottom,
                                                modifier = Modifier.height(14.dp)
                                            ) {
                                                val barHeights = listOf(4.dp, 6.dp, 8.dp, 10.dp, 12.dp)
                                                barHeights.forEach { h ->
                                                    Box(
                                                        modifier = Modifier
                                                            .width(2.5.dp)
                                                            .height(h)
                                                            .background(Color(0xFF4CAF50))
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = server.address,
                                            color = Color(0xFF666666),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Action Buttons (Matching Screenshot)
            // Row 1: [Join Server] [Direct Connection] [Add Server] [Create Free Server]
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(0.96f)
            ) {
                JavaOptionButton(
                    text = "Join Server",
                    enabled = selectedServer != null,
                    modifier = Modifier.weight(1f),
                    height = 32.dp,
                    onClick = {
                        selectedServer?.let { triggerJoin(it) }
                    }
                )

                JavaOptionButton(
                    text = "Direct Connection",
                    modifier = Modifier.weight(1f),
                    height = 32.dp,
                    onClick = {
                        soundManager.playClick()
                        showDirectConnectDialog = true
                    }
                )

                JavaOptionButton(
                    text = "Add Server",
                    modifier = Modifier.weight(1f),
                    height = 32.dp,
                    onClick = {
                        soundManager.playClick()
                        showAddServerDialog = true
                    }
                )

                // Requested: "add like create free server option"
                JavaOptionButton(
                    text = "Create Free Server",
                    modifier = Modifier.weight(1.15f),
                    height = 32.dp,
                    textColor = Color(0xFF81C784),
                    onClick = {
                        soundManager.playClick()
                        showCreateFreeServerDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Row 2: [Edit] [Delete] [Refresh] [Back] (Matching Screenshot)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(0.96f)
            ) {
                JavaOptionButton(
                    text = "Edit",
                    enabled = selectedServer != null,
                    modifier = Modifier.weight(1f),
                    height = 32.dp,
                    onClick = {
                        soundManager.playClick()
                        showEditServerDialog = true
                    }
                )

                JavaOptionButton(
                    text = "Delete",
                    enabled = selectedServer != null,
                    modifier = Modifier.weight(1f),
                    height = 32.dp,
                    onClick = {
                        soundManager.playClick()
                        showDeleteConfirmDialog = true
                    }
                )

                JavaOptionButton(
                    text = if (isRefreshing) "Refreshing..." else "Refresh",
                    modifier = Modifier.weight(1f),
                    height = 32.dp,
                    onClick = {
                        soundManager.playClick()
                        isRefreshing = true
                        coroutineScope.launch {
                            delay(600)
                            // Simulate ping refresh
                            serverList.forEachIndexed { i, s ->
                                serverList[i] = s.copy(ping = (12..35).random())
                            }
                            isRefreshing = false
                        }
                    }
                )

                JavaOptionButton(
                    text = "Back",
                    modifier = Modifier.weight(1f),
                    height = 32.dp,
                    onClick = {
                        soundManager.playClick()
                        onBack()
                    }
                )
            }
        }

        // Connecting / Loading Overlay
        if (isConnecting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xE6000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF81C784),
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = connectingStatusText,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    JavaOptionButton(
                        text = "Cancel",
                        height = 30.dp,
                        modifier = Modifier.width(130.dp),
                        onClick = {
                            isConnecting = false
                        }
                    )
                }
            }
        }

        // Dialog: Create Free Server (Requested Feature)
        if (showCreateFreeServerDialog) {
            CreateFreeServerDialog(
                soundManager = soundManager,
                onDismiss = { showCreateFreeServerDialog = false },
                onCreate = { newServer ->
                    serverList.add(0, newServer)
                    selectedServerId = newServer.id
                    ServerStorage.saveServers(context, serverList)
                    showCreateFreeServerDialog = false
                }
            )
        }

        // Dialog: Add Server
        if (showAddServerDialog) {
            EditServerInfoDialog(
                title = "Edit Server Info",
                initialName = "Minecraft Server",
                initialAddress = "",
                initialResourcePacks = "Prompt",
                soundManager = soundManager,
                onDismiss = { showAddServerDialog = false },
                onDone = { name, address, packs ->
                    val server = ServerEntry(
                        name = name.ifBlank { "Minecraft Server" },
                        address = address.ifBlank { "localhost:25565" },
                        motdLine1 = "A Minecraft Server",
                        motdLine2 = "Minecraft 26.2 Compatible",
                        resourcePackMode = packs
                    )
                    serverList.add(server)
                    selectedServerId = server.id
                    ServerStorage.saveServers(context, serverList)
                    showAddServerDialog = false
                }
            )
        }

        // Dialog: Edit Server
        if (showEditServerDialog && selectedServer != null) {
            EditServerInfoDialog(
                title = "Edit Server Info",
                initialName = selectedServer.name,
                initialAddress = selectedServer.address,
                initialResourcePacks = selectedServer.resourcePackMode,
                soundManager = soundManager,
                onDismiss = { showEditServerDialog = false },
                onDone = { name, address, packs ->
                    val idx = serverList.indexOfFirst { it.id == selectedServer.id }
                    if (idx != -1) {
                        serverList[idx] = selectedServer.copy(
                            name = name.ifBlank { selectedServer.name },
                            address = address.ifBlank { selectedServer.address },
                            resourcePackMode = packs
                        )
                        ServerStorage.saveServers(context, serverList)
                    }
                    showEditServerDialog = false
                }
            )
        }

        // Dialog: Direct Connection
        if (showDirectConnectDialog) {
            DirectConnectDialog(
                soundManager = soundManager,
                onDismiss = { showDirectConnectDialog = false },
                onConnect = { ip ->
                    showDirectConnectDialog = false
                    triggerDirectJoin(ip)
                }
            )
        }

        // Dialog: Delete Confirmation
        if (showDeleteConfirmDialog && selectedServer != null) {
            DeleteServerConfirmDialog(
                serverName = selectedServer.name,
                soundManager = soundManager,
                onDismiss = { showDeleteConfirmDialog = false },
                onConfirm = {
                    serverList.removeAll { it.id == selectedServer.id }
                    selectedServerId = null
                    ServerStorage.saveServers(context, serverList)
                    showDeleteConfirmDialog = false
                }
            )
        }
    }
}

/**
 * Dialog to create a Free 24/7 Minecraft Server.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateFreeServerDialog(
    soundManager: SoundManager,
    onDismiss: () -> Unit,
    onCreate: (ServerEntry) -> Unit
) {
    var serverName by remember { mutableStateOf("My Free SMP") }
    var subdomain by remember { mutableStateOf("survival-smp") }
    var selectedSoftware by remember { mutableStateOf("Paper 26.2") }
    var selectedRegion by remember { mutableStateOf("North America (East)") }
    var maxPlayers by remember { mutableStateOf(20) }

    var isDeploying by remember { mutableStateOf(false) }
    var deployStep by remember { mutableStateOf("Allocating cloud container...") }
    val coroutineScope = rememberCoroutineScope()

    BasicAlertDialog(onDismissRequest = { if (!isDeploying) onDismiss() }) {
        Box(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF222222))
                .border(2.dp, Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "⚡", fontSize = 18.sp)
                    Text(
                        text = "Create Free Server",
                        color = Color(0xFF81C784),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Cloud badge info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1B5E20), RoundedCornerShape(2.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Free 24/7 Hosting • 2GB RAM • Version 26.2 • Zero Cost",
                        color = Color(0xFFA5D6A7),
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (isDeploying) {
                    Spacer(modifier = Modifier.height(10.dp))
                    CircularProgressIndicator(color = Color(0xFF81C784), modifier = Modifier.size(32.dp))
                    Text(
                        text = deployStep,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                } else {
                    // Server Name Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Server Name:",
                            color = Color(0xFFAAAAAA),
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        OutlinedTextField(
                            value = serverName,
                            onValueChange = { serverName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF81C784),
                                unfocusedBorderColor = Color(0xFF666666),
                                focusedContainerColor = Color(0xFF111111),
                                unfocusedContainerColor = Color(0xFF111111)
                            )
                        )
                    }

                    // Domain / Subdomain Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Server Address:",
                            color = Color(0xFFAAAAAA),
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF111111), RoundedCornerShape(2.dp))
                                .border(1.dp, Color(0xFF666666), RoundedCornerShape(2.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${subdomain.ifBlank { "server" }}.freemc.cloud:25565",
                                color = Color(0xFF81C784),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Software Selector Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val softwares = listOf("Paper 26.2", "Purpur 26.2", "Fabric 26.2", "Vanilla 26.2")
                        JavaOptionButton(
                            text = "Software: $selectedSoftware",
                            modifier = Modifier.weight(1f),
                            height = 32.dp,
                            onClick = {
                                val nextIdx = (softwares.indexOf(selectedSoftware) + 1) % softwares.size
                                selectedSoftware = softwares[nextIdx]
                            }
                        )

                        JavaOptionButton(
                            text = "Max: $maxPlayers Players",
                            modifier = Modifier.weight(1f),
                            height = 32.dp,
                            onClick = {
                                maxPlayers = if (maxPlayers == 20) 50 else if (maxPlayers == 50) 100 else 20
                            }
                        )
                    }

                    // Actions: [Deploy Server] and [Cancel]
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        JavaOptionButton(
                            text = "Create Server",
                            modifier = Modifier.weight(1f),
                            height = 34.dp,
                            textColor = Color(0xFF81C784),
                            onClick = {
                                soundManager.playClick()
                                isDeploying = true
                                coroutineScope.launch {
                                    deployStep = "Allocating 2GB RAM container..."
                                    delay(500)
                                    deployStep = "Installing $selectedSoftware..."
                                    delay(600)
                                    deployStep = "Binding port 25565..."
                                    delay(400)
                                    deployStep = "Server Online!"
                                    delay(300)

                                    val finalServer = ServerEntry(
                                        name = serverName.ifBlank { "My Free SMP" },
                                        address = "${subdomain.ifBlank { "server" }}.freemc.cloud:25565",
                                        motdLine1 = "Free 24/7 Minecraft 26.2 Server",
                                        motdLine2 = "Welcome! Running $selectedSoftware",
                                        ping = 14,
                                        playersOnline = 0,
                                        maxPlayers = maxPlayers,
                                        isFreeServer = true,
                                        iconEmoji = "⚡"
                                    )
                                    onCreate(finalServer)
                                }
                            }
                        )

                        JavaOptionButton(
                            text = "Cancel",
                            modifier = Modifier.weight(1f),
                            height = 34.dp,
                            onClick = {
                                soundManager.playClick()
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Authentic Minecraft Java Edition "Edit Server Info" dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditServerInfoDialog(
    title: String,
    initialName: String,
    initialAddress: String,
    initialResourcePacks: String,
    soundManager: SoundManager,
    onDismiss: () -> Unit,
    onDone: (name: String, address: String, packs: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var address by remember { mutableStateOf(initialAddress) }
    var resourcePacks by remember { mutableStateOf(initialResourcePacks) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(380.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF222222))
                .border(2.dp, Color(0xFF666666), RoundedCornerShape(4.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Server Name",
                        color = Color(0xFFAAAAAA),
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF81C784),
                            unfocusedBorderColor = Color(0xFF666666),
                            focusedContainerColor = Color(0xFF111111),
                            unfocusedContainerColor = Color(0xFF111111)
                        )
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Server Address",
                        color = Color(0xFFAAAAAA),
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        placeholder = { Text("e.g. mc.hypixel.net", color = Color(0xFF666666), fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF81C784),
                            unfocusedBorderColor = Color(0xFF666666),
                            focusedContainerColor = Color(0xFF111111),
                            unfocusedContainerColor = Color(0xFF111111)
                        )
                    )
                }

                JavaOptionButton(
                    text = "Server Resource Packs: $resourcePacks",
                    modifier = Modifier.fillMaxWidth(),
                    height = 32.dp,
                    onClick = {
                        resourcePacks = when (resourcePacks) {
                            "Prompt" -> "Enabled"
                            "Enabled" -> "Disabled"
                            else -> "Prompt"
                        }
                    }
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    JavaOptionButton(
                        text = "Done",
                        modifier = Modifier.weight(1f),
                        height = 32.dp,
                        onClick = {
                            soundManager.playClick()
                            onDone(name, address, resourcePacks)
                        }
                    )

                    JavaOptionButton(
                        text = "Cancel",
                        modifier = Modifier.weight(1f),
                        height = 32.dp,
                        onClick = {
                            soundManager.playClick()
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Authentic Minecraft Java Edition "Direct Connection" dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DirectConnectDialog(
    soundManager: SoundManager,
    onDismiss: () -> Unit,
    onConnect: (String) -> Unit
) {
    var ipAddress by remember { mutableStateOf("") }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF222222))
                .border(2.dp, Color(0xFF666666), RoundedCornerShape(4.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Direct Connection",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Enter the IP of a server to connect directly to it:",
                    color = Color(0xFFAAAAAA),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { ipAddress = it },
                    placeholder = { Text("localhost:25565", color = Color(0xFF666666), fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF81C784),
                        unfocusedBorderColor = Color(0xFF666666),
                        focusedContainerColor = Color(0xFF111111),
                        unfocusedContainerColor = Color(0xFF111111)
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    JavaOptionButton(
                        text = "Join Server",
                        modifier = Modifier.weight(1f),
                        height = 32.dp,
                        onClick = {
                            soundManager.playClick()
                            onConnect(ipAddress.ifBlank { "localhost:25565" })
                        }
                    )

                    JavaOptionButton(
                        text = "Cancel",
                        modifier = Modifier.weight(1f),
                        height = 32.dp,
                        onClick = {
                            soundManager.playClick()
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Authentic Minecraft Java Edition "Delete Server" confirmation dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteServerConfirmDialog(
    serverName: String,
    soundManager: SoundManager,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF222222))
                .border(2.dp, Color(0xFFC62828), RoundedCornerShape(4.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Are you sure you want to remove this server?",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "'$serverName' will be lost forever! (A long time!)",
                    color = Color(0xFFFF8A80),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    JavaOptionButton(
                        text = "Delete",
                        modifier = Modifier.weight(1f),
                        height = 32.dp,
                        textColor = Color(0xFFFF5252),
                        onClick = {
                            soundManager.playClick()
                            onConfirm()
                        }
                    )

                    JavaOptionButton(
                        text = "Cancel",
                        modifier = Modifier.weight(1f),
                        height = 32.dp,
                        onClick = {
                            soundManager.playClick()
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}
