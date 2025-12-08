package com.example.auditshield.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.auditshield.database.dao.AuditDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.auditshield.database.AppDatabase
import com.example.auditshield.database.entity.QuestionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "audit.db"
        )
        .fallbackToDestructiveMigration() // reset automático mientras estás desarrollando
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                // Cargar preguntas iniciales DESPUÉS de que Room cree la BD
                CoroutineScope(Dispatchers.IO).launch {
                    val database = Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        "audit.db"
                    ).build()

                    val questionDao = database.questionDao()

                    questionDao.insertAll(
                        listOf(
                            // A.5 Políticas de Seguridad
                            QuestionEntity(text = "¿Existe una política de seguridad aprobada por la dirección?", controlRef = "A.5"),
                            QuestionEntity(text = "¿La política de seguridad se revisa periódicamente?", controlRef = "A.5"),

                            // A.6 Organización de la seguridad
                            QuestionEntity(text = "¿Los roles y responsabilidades de seguridad están claramente definidos?", controlRef = "A.6"),
                            QuestionEntity(text = "¿Existe segregación de funciones para evitar fraude o abuso?", controlRef = "A.6"),

                            // A.7 Seguridad en recursos humanos
                            QuestionEntity(text = "¿El personal firma acuerdos de confidencialidad?", controlRef = "A.7"),
                            QuestionEntity(text = "¿El personal recibe capacitación periódica en seguridad?", controlRef = "A.7"),

                            // A.8 Gestión de activos
                            QuestionEntity(text = "¿Existe un inventario actualizado de activos?", controlRef = "A.8"),
                            QuestionEntity(text = "¿Los activos tienen un propietario asignado?", controlRef = "A.8"),

                            // A.9 Control de acceso
                            QuestionEntity(text = "¿El acceso se otorga siguiendo el principio de mínimo privilegio?", controlRef = "A.9"),
                            QuestionEntity(text = "¿Las cuentas inactivas se eliminan o deshabilitan a tiempo?", controlRef = "A.9"),

                            // A.10 Criptografía
                            QuestionEntity(text = "¿Se usa criptografía aprobada en datos sensibles?", controlRef = "A.10"),
                            QuestionEntity(text = "¿Las claves criptográficas se gestionan correctamente?", controlRef = "A.10"),

                            // A.11 Seguridad física
                            QuestionEntity(text = "¿El acceso físico a las áreas críticas está controlado?", controlRef = "A.11"),
                            QuestionEntity(text = "¿Hay mecanismos de protección contra incendios o inundaciones?", controlRef = "A.11"),

                            // A.12 Seguridad operativa
                            QuestionEntity(text = "¿Existe segregación entre entornos de desarrollo, prueba y producción?", controlRef = "A.12"),
                            QuestionEntity(text = "¿Los registros de actividad se monitorean regularmente?", controlRef = "A.12"),

                            // A.13 Seguridad en comunicaciones
                            QuestionEntity(text = "¿Las redes están segmentadas según criticidad?", controlRef = "A.13"),
                            QuestionEntity(text = "¿La información transmitida está protegida adecuadamente?", controlRef = "A.13"),

                            // A.14 Desarrollo y adquisición
                            QuestionEntity(text = "¿Las aplicaciones pasan por pruebas de seguridad antes de publicarse?", controlRef = "A.14"),
                            QuestionEntity(text = "¿Los cambios en software siguen un proceso de control formal?", controlRef = "A.14"),

                            // A.15 Proveedores
                            QuestionEntity(text = "¿Los contratos con proveedores incluyen requisitos de seguridad?", controlRef = "A.15"),
                            QuestionEntity(text = "¿Se evalúa el riesgo de los proveedores críticos?", controlRef = "A.15"),

                            // A.16 Gestión de incidentes
                            QuestionEntity(text = "¿Existe un proceso formal para reportar incidentes?", controlRef = "A.16"),
                            QuestionEntity(text = "¿Los incidentes son analizados para prevenir recurrencias?", controlRef = "A.16"),

                            // A.17 Continuidad de negocio
                            QuestionEntity(text = "¿Existen planes de continuidad documentados?", controlRef = "A.17"),
                            QuestionEntity(text = "¿Los planes se prueban periódicamente?", controlRef = "A.17"),

                            // A.18 Cumplimiento
                            QuestionEntity(text = "¿La organización cumple con leyes y normas aplicables?", controlRef = "A.18"),
                            QuestionEntity(text = "¿Se realiza monitoreo para detectar incumplimientos?", controlRef = "A.18")
                        )
                    )
                }
            }
        })
        .build()
    }


    @Provides
    fun provideAuditDao(db: AppDatabase) = db.auditDao()

    @Provides
    fun provideQuestionDao(db: AppDatabase) = db.questionDao()

    @Provides
    fun provideAnswerDao(db: AppDatabase) = db.answerDao()
}
