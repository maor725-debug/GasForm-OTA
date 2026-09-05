package com.example.myapplication158.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [GasForm::class, PeriodicGasForm::class], version = 18, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gasFormDao(): GasFormDao
    abstract fun periodicGasFormDao(): PeriodicGasFormDao // הגישה לטפסים החדשים

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE gas_forms ADD COLUMN remarksImageUris TEXT")
            }
        }

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE gas_forms ADD COLUMN sequentialNumber INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE gas_forms ADD COLUMN nonCompliantReason TEXT NOT NULL DEFAULT ''")
            }
        }

        // המיגרציה: יצירת טבלת הטפסים התקופתיים מאפס מבלי לגעת בטפסים הנורמטיביים
        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `periodic_gas_forms` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `sequentialNumber` INTEGER NOT NULL, 
                        `date` TEXT NOT NULL, 
                        `clientName` TEXT NOT NULL, 
                        `clientPhone` TEXT NOT NULL, 
                        `businessName` TEXT NOT NULL, 
                        `businessType` TEXT NOT NULL, 
                        `businessId` TEXT NOT NULL, 
                        `fireDeptFileNumber` TEXT NOT NULL, 
                        `isUnaddressedSite` INTEGER NOT NULL, 
                        `gpsCoordinates` TEXT NOT NULL, 
                        `sitePhotoUri` TEXT NOT NULL, 
                        `city` TEXT NOT NULL, 
                        `street` TEXT NOT NULL, 
                        `building` TEXT NOT NULL, 
                        `zipCode` TEXT NOT NULL, 
                        `poBox` TEXT NOT NULL, 
                        `contactName` TEXT NOT NULL, 
                        `contactRole` TEXT NOT NULL, 
                        `contactPhone` TEXT NOT NULL, 
                        `contactEmail` TEXT NOT NULL, 
                        `gasProvider` TEXT NOT NULL, 
                        `consumersCount` TEXT NOT NULL, 
                        `cylindersCount` TEXT NOT NULL, 
                        `manifoldNumber` TEXT NOT NULL, 
                        `checkLocationOpen` TEXT NOT NULL, 
                        `checkSafetyDistances` TEXT NOT NULL, 
                        `checkRegulatorSecured` TEXT NOT NULL, 
                        `checkWarningSigns` TEXT NOT NULL, 
                        `checkWaterSprinklers` TEXT NOT NULL, 
                        `checkGasRoomMax20` TEXT NOT NULL, 
                        `checkGasRoomLighting` TEXT NOT NULL, 
                        `checkGasRoomNoFlammables` TEXT NOT NULL, 
                        `checkCageMax20` TEXT NOT NULL, 
                        `checkCageVentilated` TEXT NOT NULL, 
                        `checkRampsSecured` TEXT NOT NULL, 
                        `checkEarthquakeValve` TEXT NOT NULL, 
                        `checkEarthquakeValveSecured` TEXT NOT NULL, 
                        `checkMainValveAccessible` TEXT NOT NULL, 
                        `checkDischargeValves` TEXT NOT NULL, 
                        `checkPressureUpTo1_4` TEXT NOT NULL, 
                        `checkPipingSecured` TEXT NOT NULL, 
                        `checkUnusedOutletsPlugged` TEXT NOT NULL, 
                        `failedReasonsJson` TEXT NOT NULL, 
                        `isLeakFoundPrimary` INTEGER NOT NULL, 
                        `leakLocationDetails` TEXT NOT NULL, 
                        `intermediatePressureValue` TEXT NOT NULL, 
                        `isIntermediatePressureKept` INTEGER NOT NULL, 
                        `finalStatus` TEXT NOT NULL, 
                        `defectsFixByDate` TEXT NOT NULL, 
                        `executionRemarks` TEXT NOT NULL, 
                        `technicianName` TEXT NOT NULL, 
                        `technicianLicense` TEXT NOT NULL, 
                        `technicianSignatureUri` TEXT NOT NULL, 
                        `clientNameConfirm` TEXT NOT NULL, 
                        `clientSignatureUri` TEXT NOT NULL, 
                        `extraImagesUris` TEXT NOT NULL, 
                        `createdAt` INTEGER NOT NULL, 
                        `savedTargetLocation` TEXT, 
                        `isSavedToTarget` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        // מיגרציה 18: הוספת 6 עמודות לשאלות מרחקי הבטיחות הספציפיות בטופס ד-1
        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE periodic_gas_forms ADD COLUMN checkSafetyDistances07Heat TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE periodic_gas_forms ADD COLUMN checkSafetyDistances17Fire TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE periodic_gas_forms ADD COLUMN checkSafetyDistances05Pits TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE periodic_gas_forms ADD COLUMN checkSafetyDistances3Drainage TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE periodic_gas_forms ADD COLUMN checkSafetyDistances12Building TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE periodic_gas_forms ADD COLUMN checkSafetyDistances3LowLevel TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gas_forms_database"
                )
                    .addMigrations(MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}