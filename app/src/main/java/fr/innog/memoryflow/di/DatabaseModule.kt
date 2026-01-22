package fr.innog.memoryflow.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.innog.memoryflow.data.dao.CardDao
import fr.innog.memoryflow.data.dao.DeckDao
import fr.innog.memoryflow.data.database.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, "memoryflow_db")
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideDeckDao(db: AppDatabase): DeckDao = db.deckDao()

    @Provides
    fun provideCardDao(db: AppDatabase): CardDao = db.cardDao()


}