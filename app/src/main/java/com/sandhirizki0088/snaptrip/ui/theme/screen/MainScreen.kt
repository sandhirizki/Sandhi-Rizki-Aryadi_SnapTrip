package com.sandhirizki0088.snaptrip.ui.theme.screen


import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest

import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.sandhirizki0088.snaptrip.BuildConfig
import com.sandhirizki0088.snaptrip.R
import com.sandhirizki0088.snaptrip.model.Travel
import com.sandhirizki0088.snaptrip.model.User
import com.sandhirizki0088.snaptrip.network.ApiStatus
import com.sandhirizki0088.snaptrip.network.UserDataStore
import com.sandhirizki0088.snaptrip.ui.theme.SnapTripTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User())

    val viewModel: MainViewModel = viewModel()
    val errorMessage by viewModel.errorMessage

    var showDialog by remember { mutableStateOf(false) }
    var showTravelDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    var selectedTravel by remember { mutableStateOf<Travel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var bitmap: Bitmap? by remember { mutableStateOf(null) }

    val launcherFromGallery = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bmp = loadBitmapFromUri(context, it)
            if (bmp != null) {
                bitmap = bmp
                showTravelDialog = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = {
                        if (user.email.isEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch { signIn(context, dataStore) }
                        }
                        else {
                            showDialog = true
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_account_circle_24),
                            contentDescription = stringResource(R.string.profil),
                            tint = MaterialTheme.colorScheme.primary

                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if(user.email != "") {
                FloatingActionButton(onClick = {
                    launcherFromGallery.launch("image/*")
                }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.tambah_hewan)
                    )
                }
            }
        }
    ) { innerPadding ->
        ScreenContent(viewModel, user.email, Modifier.padding(innerPadding),  onDeleteClick = { Travel ->
            selectedTravel = Travel
            showDeleteDialog = true
        },
            onEditClick = {travel ->
                selectedTravel = travel
                showEditDialog = true
            }

        )
        if (showDialog){
            ProfilDialog(
                user = user,
                onDismissRequest = {showDialog = false }
            ) {
                CoroutineScope(Dispatchers.IO).launch {  signOut(context, dataStore) }
                showDialog = false
            }
        }
        if (showTravelDialog) {
            TravelDialog(
                bitmap = bitmap,
                userId = user.email,
                onDismissRequest = { showTravelDialog = false },
                onConfirmation = { tempat, tanggal, userId, bitmap ->
                    viewModel.uploadAndSave(
                         tempat, tanggal, userId, bitmap,
                        onSuccess = {
                            Toast.makeText(context, "Data disimpan!", Toast.LENGTH_SHORT).show()
                            showTravelDialog = false
                        },
                        onError = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }

        if (showDeleteDialog && selectedTravel != null) {
            DeleteDialog(
                travel = selectedTravel!!,
                user = user,
                onDismissRequest = { showDeleteDialog = false },
                onConfirmation = {
                    viewModel.deleteData(user.email, selectedTravel!!.id)
                    showDeleteDialog = false
                    selectedTravel = null
                }
            )
        }

        if (showEditDialog && selectedTravel != null) {
            val travel = selectedTravel
            if (travel != null) {
                EditDialog(
                    travel = travel,
                    imageId = travel.imageId,
                    userId = user.email,
                    onDismissRequest = { showEditDialog = false },
                    onConfirmation = {tempat,tanggal,imageId, userId ->
                        viewModel.updateData(
                            id = travel.id,
                            tempat= tempat,
                            tanggal=tanggal,
                            imageId=imageId,
                            userId = userId

                        )
                        Toast.makeText(context, "Info berhasil diubah", Toast.LENGTH_SHORT).show()
                        showEditDialog = false
                    },
                    isEdit = true
                )
            }
        }


        if (errorMessage != null){
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }
}

fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        val stream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(stream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun ScreenContent(viewModel: MainViewModel,userId: String,modifier: Modifier = Modifier,
                  onDeleteClick: (Travel) -> Unit,
                  onEditClick: (Travel) -> Unit,
){
    val data by viewModel.data
    val status by viewModel.status.collectAsState()

    LaunchedEffect(userId) {
        viewModel.retrieveData(userId)
    }

    when (status) {
        ApiStatus.LOADING -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                CircularProgressIndicator()
            }
        }

        ApiStatus.SUCCESS -> {
            LazyVerticalGrid(
                modifier = modifier.fillMaxSize().padding(4.dp),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(data) { travel ->
                    ListItem(
                        travel = travel,
                        onDeleteClick = { onDeleteClick(travel) },
                        onEditClick = { onEditClick (travel)}
                    )
                }
            }
        }

        ApiStatus.FAILED -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.error))
                Button(
                    onClick = { viewModel.retrieveData(userId)},
                    modifier = Modifier.padding(top = 16.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(text = stringResource(id = R.string.try_again))
                }
            }
        }
    }
}

@Composable
fun ListItem(travel: Travel,  onDeleteClick: () -> Unit, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(travel.imageId)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.gambar, travel.id),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.loading_img),
            error = painterResource(id = R.drawable.baseline_broken_image_24),
            modifier = Modifier.fillMaxWidth().aspectRatio(1.5f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = travel.id,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "TEMPAT : \n${travel.tempat}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "TANGGAL: \n${travel.tanggal}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                    maxLines = 5
                )
                if (travel.mine != 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { onDeleteClick() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.hapus)
                            )
                        }

                        IconButton(onClick = { onEditClick() }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(id = R.string.edit)
                            )
                        }
                    }
                }
            }
        }
    }
}


private suspend fun signIn(context: Context, dataStore: UserDataStore) {
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore)
    } catch (e: GetCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(result: GetCredentialResponse, dataStore: UserDataStore) {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
            val nama = googleId.displayName ?: ""
            val email = googleId.id
            val photoUrl = googleId.profilePictureUri.toString()
            dataStore.saveData(User(nama, email, photoUrl))
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    } else {
        Log.e("SIGN-IN", "Error: unrecognized custom credential type.")
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore){
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        dataStore.saveData(User())
    } catch (e: ClearCredentialException){
        Log.d("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun MainScreenPreview() {
    SnapTripTheme {
        MainScreen()
    }
}