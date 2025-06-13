package com.sandhirizki0088.snaptrip.ui.theme.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.canhub.cropper.CropImage.CancelledResult.bitmap
import com.sandhirizki0088.snaptrip.R
import com.sandhirizki0088.snaptrip.model.Travel


@Composable
fun EditDialog(
    travel: Travel? = null,
    imageId: String,
    userId: String,
    onDismissRequest: () -> Unit,
    onConfirmation: ( String, String, String, String) -> Unit,
    isEdit: Boolean = false,

    ){

    var tempat by remember { mutableStateOf(travel?.tempat ?: "") }
    var tanggal by remember { mutableStateOf(travel?.tanggal ?: "") }

    val context = LocalContext.current

    Dialog(onDismissRequest = {onDismissRequest()}) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ){
                AsyncImage(
                    model = imageId,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )


                OutlinedTextField(
                    value = tempat,
                    onValueChange = { tempat = it },
                    label = { Text(text = stringResource(id = R.string.nama))},
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = tanggal,
                    onValueChange = { tanggal = it },
                    label = { Text(text = stringResource(id = R.string.tanggal)) },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = stringResource(R.string.batal))
                    }
                    OutlinedButton(
                        onClick = {
                            if (isEdit) {
                                onConfirmation(tempat, tanggal, userId, imageId)
                            } else {
                                if (bitmap != null) {
                                    onConfirmation(tempat,tanggal, userId, imageId)
                                } else {
                                    Toast.makeText(context, "Gambar belum tersedia", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = tempat.isNotEmpty() && tanggal.isNotEmpty(),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = stringResource(R.string.simpan))
                    }
                }
            }
        }
    }
}