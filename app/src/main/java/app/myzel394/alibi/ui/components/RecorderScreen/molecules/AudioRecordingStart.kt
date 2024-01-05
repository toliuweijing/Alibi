package app.myzel394.alibi.ui.components.RecorderScreen.molecules

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.SUPPORTS_SCOPED_STORAGE
import app.myzel394.alibi.ui.components.atoms.PermissionRequester
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.utils.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AudioRecordingStart(
    audioRecorder: AudioRecorderModel,
    appSettings: AppSettings,
) {
    val context = LocalContext.current

    // We can't get the current `notificationDetails` inside the
    // `onPermissionAvailable` function. We'll instead use this hack
    // with `LaunchedEffect` to get the current value.
    var startRecording by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(startRecording) {
        if (startRecording) {
            startRecording = false

            audioRecorder.startRecording(context, appSettings)
        }
    }

    println("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww app: ${appSettings.saveFolder}")
    val requiresExternalPerm = rememberSaveable {
        appSettings.requiresExternalStoragePermission(context)
    }
    println("hasGranted ${requiresExternalPerm}")
    val scope = rememberCoroutineScope()

    fun test() {
        println("appSäääääääääääääääääääääääääääääääättings ${appSettings.saveFolder}")
    }

    PermissionRequester(
        permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
        icon = Icons.Default.InsertDriveFile,
        onPermissionAvailable = {
            startRecording = true
        }
    ) { triggerExternalStorage ->
        PermissionRequester(
            permission = Manifest.permission.RECORD_AUDIO,
            icon = Icons.Default.Mic,
            onPermissionAvailable = {
                test()
                if (appSettings.requiresExternalStoragePermission(context)) {
                    triggerExternalStorage()
                } else {
                    startRecording = true
                }
            }
        ) { triggerRecordAudio ->
            val label = stringResource(R.string.ui_audioRecorder_action_start_label)

            Button(
                onClick = triggerRecordAudio,
                modifier = Modifier
                    .semantics {
                        contentDescription = label
                    }
                    .size(250.dp)
                    .clip(shape = CircleShape),
                colors = ButtonDefaults.outlinedButtonColors(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp),
                    )
                    Spacer(modifier = Modifier.height(ButtonDefaults.IconSpacing))
                    Text(
                        label,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }
    }
}