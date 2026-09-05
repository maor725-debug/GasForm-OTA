package com.example.myapplication158.data

import kotlinx.coroutines.flow.Flow

class GasFormRepository(
    private val gasFormDao: GasFormDao,
    private val periodicGasFormDao: PeriodicGasFormDao
) {
    // --- נתיב 1: הטפסים הנורמטיביים (קלאסי/מודרני) ---
    val allForms: Flow<List<GasForm>> = gasFormDao.getAllForms()

    suspend fun getFormById(id: Int): GasForm? {
        return gasFormDao.getFormById(id)
    }

    suspend fun insert(form: GasForm): Long {
        return gasFormDao.insertForm(form)
    }

    suspend fun update(form: GasForm) {
        gasFormDao.updateForm(form)
    }

    suspend fun delete(form: GasForm) {
        gasFormDao.deleteForm(form)
    }

    // --- נתיב 2: הטפסים התקופתיים (ד-1) ---
    val allPeriodicForms: Flow<List<PeriodicGasForm>> = periodicGasFormDao.getAllForms()

    suspend fun getPeriodicFormById(id: Int): PeriodicGasForm? {
        return periodicGasFormDao.getFormById(id)
    }

    suspend fun insertPeriodic(form: PeriodicGasForm): Long {
        return periodicGasFormDao.insertForm(form)
    }

    suspend fun updatePeriodic(form: PeriodicGasForm) {
        periodicGasFormDao.updateForm(form)
    }

    suspend fun deletePeriodic(form: PeriodicGasForm) {
        periodicGasFormDao.deleteForm(form)
    }
}