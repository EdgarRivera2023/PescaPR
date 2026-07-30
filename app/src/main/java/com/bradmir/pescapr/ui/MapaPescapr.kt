package com.bradmir.pescapr.ui

import com.bradmir.pescapr.data.PuntoPesca
import com.bradmir.pescapr.ui.viewmodels.MapViewModel
import com.google.firebase.auth.FirebaseAuth

fun shareSpotToCommunity(viewModel: MapViewModel, spot: PuntoPesca) {
    val uid = spot.userId.ifBlank { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    viewModel.shareSpotToCommunity(spot.copy(userId = uid))
}
