package com.example.auditshield.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auditshield.repository.AuditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AuditRepository
) : ViewModel() {

    val audits = repository.getAllAudits().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun createNewAudit(title: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createAudit(title)
            onCreated(id)
        }
    }
}

