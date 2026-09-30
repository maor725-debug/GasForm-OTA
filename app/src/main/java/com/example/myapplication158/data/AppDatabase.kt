package com.example.myapplication158.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [GasForm::class, PeriodicGasForm::class, GasFormD2::class, GasFormD3::class, GasFormD4::class, WorkOrder::class], version = 23, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gasFormDao(): GasFormDao
    abstract fun periodicGasFormDao(): PeriodicGasFormDao
    abstract fun gasFormD2Dao(): GasFormD2Dao
    abstract fun gasFormD3Dao(): GasFormD3Dao
    abstract fun gasFormD4Dao(): GasFormD4Dao
    abstract fun workOrderDao(): WorkOrderDao

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

        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `work_orders` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `targetDate` TEXT NOT NULL,
                        `targetTime` TEXT NOT NULL,
                        `location` TEXT NOT NULL,
                        `clientPhone` TEXT NOT NULL,
                        `jobDescription` TEXT NOT NULL,
                        `quotedPrice` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `isMuted` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `GasFormD2` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sequentialNumber` INTEGER NOT NULL,
                        `date` TEXT NOT NULL,
                        `businessName` TEXT NOT NULL,
                        `businessType` TEXT NOT NULL,
                        `businessId` TEXT NOT NULL,
                        `fireDeptFileNumber` TEXT NOT NULL,
                        `city` TEXT NOT NULL,
                        `street` TEXT NOT NULL,
                        `building` TEXT NOT NULL,
                        `clientName` TEXT NOT NULL,
                        `clientPhone` TEXT NOT NULL,
                        `gasProvider` TEXT NOT NULL,
                        `isUnaddressedSite` INTEGER NOT NULL,
                        `gpsCoordinates` TEXT NOT NULL,
                        `sitePhotoUri` TEXT NOT NULL,
                        `manufactureOrTestYear` TEXT NOT NULL,
                        `capacityPerTank` TEXT NOT NULL,
                        `totalCapacity` TEXT NOT NULL,
                        `tankType` TEXT NOT NULL,
                        `usageType` TEXT NOT NULL,
                        `manifoldNumber` TEXT NOT NULL,
                        `suppliesToBuildings` TEXT NOT NULL,
                        `checkSiteSignage` TEXT NOT NULL,
                        `checkSiteClean` TEXT NOT NULL,
                        `checkTankPlate` TEXT NOT NULL,
                        `checkTankCover` TEXT NOT NULL,
                        `checkTankFittings` TEXT NOT NULL,
                        `checkSafeAccess` TEXT NOT NULL,
                        `checkFittingsHeight` TEXT NOT NULL,
                        `checkSafetyDistances` TEXT NOT NULL,
                        `checkElecDistances` TEXT NOT NULL,
                        `checkFillPipe` TEXT NOT NULL,
                        `checkEarthquakeValve` TEXT NOT NULL,
                        `checkValveLevel` TEXT NOT NULL,
                        `checkMainValve` TEXT NOT NULL,
                        `checkDischargeValve` TEXT NOT NULL,
                        `checkPressure1_4` TEXT NOT NULL,
                        `checkPipingSecured` TEXT NOT NULL,
                        `checkOutletsPlugged` TEXT NOT NULL,
                        `isLeakFoundPrimary` INTEGER NOT NULL,
                        `leakLocationDetails` TEXT NOT NULL,
                        `intermediatePressureValue` TEXT NOT NULL,
                        `isIntermediatePressureKept` INTEGER NOT NULL,
                        `finalStatus` TEXT NOT NULL,
                        `defectsFixByDate` TEXT NOT NULL,
                        `executionRemarks` TEXT NOT NULL,
                        `technicianName` TEXT NOT NULL,
                        `technicianLicense` TEXT NOT NULL,
                        `clientNameConfirm` TEXT NOT NULL,
                        `clientSignatureUri` TEXT NOT NULL,
                        `extraImagesUris` TEXT NOT NULL,
                        `failedReasonsJson` TEXT NOT NULL,
                        `savedTargetLocation` TEXT NOT NULL,
                        `isSavedToTarget` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `GasFormD3` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sequentialNumber` INTEGER NOT NULL,
                        `date` TEXT NOT NULL,
                        `consumerNumber` TEXT NOT NULL,
                        `consumerMeterNumber` TEXT NOT NULL,
                        `meterManufactureYear` TEXT NOT NULL,
                        `clientName` TEXT NOT NULL,
                        `city` TEXT NOT NULL,
                        `street` TEXT NOT NULL,
                        `gasProvider` TEXT NOT NULL,
                        `facilityType` TEXT NOT NULL,
                        `businessId` TEXT NOT NULL,
                        `businessName` TEXT NOT NULL,
                        `businessType` TEXT NOT NULL,
                        `fireDeptFileNumber` TEXT NOT NULL,
                        `poBox` TEXT NOT NULL,
                        `zip` TEXT NOT NULL,
                        `building` TEXT NOT NULL,
                        `mainContactName` TEXT NOT NULL,
                        `contactRole` TEXT NOT NULL,
                        `clientPhone` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `check1_1` TEXT NOT NULL,
                        `check1_2` TEXT NOT NULL,
                        `check1_3` TEXT NOT NULL,
                        `check1_4` TEXT NOT NULL,
                        `check1_5` TEXT NOT NULL,
                        `check1_6` TEXT NOT NULL,
                        `check1_7` TEXT NOT NULL,
                        `check1_8` TEXT NOT NULL,
                        `check1_9` TEXT NOT NULL,
                        `devicesList` TEXT NOT NULL,
                        `check2_1` TEXT NOT NULL,
                        `check2_2` TEXT NOT NULL,
                        `check2_3` TEXT NOT NULL,
                        `check2_4` TEXT NOT NULL,
                        `check2_5` TEXT NOT NULL,
                        `check2_6_1` TEXT NOT NULL,
                        `check2_6_2` TEXT NOT NULL,
                        `check2_6_3` TEXT NOT NULL,
                        `check2_6_4` TEXT NOT NULL,
                        `check2_6_5` TEXT NOT NULL,
                        `check2_7_1` TEXT NOT NULL,
                        `check2_7_2` TEXT NOT NULL,
                        `check2_7_3` TEXT NOT NULL,
                        `check2_8_1` TEXT NOT NULL,
                        `check2_8_2` TEXT NOT NULL,
                        `check2_9` TEXT NOT NULL,
                        `check3_1` TEXT NOT NULL,
                        `testPressure` TEXT NOT NULL,
                        `check3_2` TEXT NOT NULL,
                        `isFacilityValid` INTEGER NOT NULL,
                        `requiresFixes` INTEGER NOT NULL,
                        `fixByDate` TEXT NOT NULL,
                        `isDisconnected` INTEGER NOT NULL,
                        `disconnectReason` TEXT NOT NULL,
                        `additionalNotes` TEXT NOT NULL,
                        `failedReasonsJson` TEXT NOT NULL,
                        `extraImagesUris` TEXT NOT NULL,
                        `technicianSignatureUri` TEXT NOT NULL,
                        `clientSignatureUri` TEXT NOT NULL,
                        `savedTargetLocation` TEXT NOT NULL,
                        `isSavedToTarget` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `GasFormD4` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sequentialNumber` INTEGER NOT NULL,
                        `date` TEXT NOT NULL,
                        `consumerNumber` TEXT NOT NULL,
                        `gasProvider` TEXT NOT NULL,
                        `facilityType` TEXT NOT NULL,
                        `activationDate` TEXT NOT NULL,
                        `businessId` TEXT NOT NULL,
                        `businessName` TEXT NOT NULL,
                        `businessType` TEXT NOT NULL,
                        `fireDeptFileNumber` TEXT NOT NULL,
                        `city` TEXT NOT NULL,
                        `street` TEXT NOT NULL,
                        `building` TEXT NOT NULL,
                        `zip` TEXT NOT NULL,
                        `poBox` TEXT NOT NULL,
                        `mainContactName` TEXT NOT NULL,
                        `contactRole` TEXT NOT NULL,
                        `clientPhone` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `clientName` TEXT NOT NULL,
                        `check1_1_1` TEXT NOT NULL,
                        `check1_1_2_1` TEXT NOT NULL,
                        `check1_1_2_2` TEXT NOT NULL,
                        `check1_1_2_3` TEXT NOT NULL,
                        `check1_1_2_4` TEXT NOT NULL,
                        `check1_1_2_5` TEXT NOT NULL,
                        `check1_1_2_6` TEXT NOT NULL,
                        `check1_2` TEXT NOT NULL,
                        `check1_3` TEXT NOT NULL,
                        `check1_4` TEXT NOT NULL,
                        `check1_5_1` TEXT NOT NULL,
                        `check1_5_2` TEXT NOT NULL,
                        `check1_5_3` TEXT NOT NULL,
                        `check1_6_1` TEXT NOT NULL,
                        `check1_6_2` TEXT NOT NULL,
                        `check1_7` TEXT NOT NULL,
                        `check2_1_1` TEXT NOT NULL,
                        `check2_2_1` TEXT NOT NULL,
                        `check3_1` TEXT NOT NULL,
                        `check3_2` TEXT NOT NULL,
                        `check3_3` TEXT NOT NULL,
                        `check3_4` TEXT NOT NULL,
                        `check3_5` TEXT NOT NULL,
                        `check3_6` TEXT NOT NULL,
                        `check3_7` TEXT NOT NULL,
                        `check3_8` TEXT NOT NULL,
                        `check3_9` TEXT NOT NULL,
                        `devicesList` TEXT NOT NULL,
                        `check4_2` TEXT NOT NULL,
                        `check4_3` TEXT NOT NULL,
                        `check4_4` TEXT NOT NULL,
                        `check4_5_1` TEXT NOT NULL,
                        `check4_5_2` TEXT NOT NULL,
                        `check4_5_3` TEXT NOT NULL,
                        `check4_5_4` TEXT NOT NULL,
                        `check4_6` TEXT NOT NULL,
                        `check4_7_1` TEXT NOT NULL,
                        `check4_7_2` TEXT NOT NULL,
                        `check4_7_3` TEXT NOT NULL,
                        `check4_8_1` TEXT NOT NULL,
                        `check4_8_2` TEXT NOT NULL,
                        `check4_9` TEXT NOT NULL,
                        `testPressure` TEXT NOT NULL,
                        `check5_1` TEXT NOT NULL,
                        `check5_2` TEXT NOT NULL,
                        `isFacilityValid` INTEGER NOT NULL,
                        `requiresFixes` INTEGER NOT NULL,
                        `fixByDate` TEXT NOT NULL,
                        `isDisconnected` INTEGER NOT NULL,
                        `disconnectReason` TEXT NOT NULL,
                        `additionalNotes` TEXT NOT NULL,
                        `failedReasonsJson` TEXT NOT NULL,
                        `extraImagesUris` TEXT NOT NULL,
                        `technicianSignatureUri` TEXT NOT NULL,
                        `clientSignatureUri` TEXT NOT NULL,
                        `clientNameConfirm` TEXT NOT NULL,
                        `savedTargetLocation` TEXT NOT NULL,
                        `isSavedToTarget` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        // הגירה לגרסה 23: הוספת שדות חסרים לד-4 (שאלות 2.1, 2.2 ותמונת מתקן)
        private val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE GasFormD4 ADD COLUMN check2_1 TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE GasFormD4 ADD COLUMN check2_2 TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE GasFormD4 ADD COLUMN facilityPhotoUri TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gas_forms_database"
                )
                    .addMigrations(
                        MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18,
                        MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}