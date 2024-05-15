package es.itg.tourismar.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.itg.tourismar.data.repository.anchorRepository.AnchorRepository
import es.itg.tourismar.data.repository.anchorRepository.AnchorRepositoryImpl
import es.itg.tourismar.data.repository.storageRepository.StorageRepository
import es.itg.tourismar.data.repository.storageRepository.StorageRepositoryImpl
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnchorModule {

    @Provides
    @Named("AnchorFirebaseFirestore")
    fun providesFirebaseFirestore() = FirebaseFirestore.getInstance()

    @Provides
    fun providesFirebaseStorage() = FirebaseStorage.getInstance()

    @Provides
    fun providesAnchorRepositoryImpl(@Named("AnchorFirebaseFirestore") firebaseFirestore: FirebaseFirestore): AnchorRepository {
        return AnchorRepositoryImpl(firebaseFirestore)
    }

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    fun providesStorageRepositoryImpl(firebaseStorage: FirebaseStorage,context: Context): StorageRepository {
        return StorageRepositoryImpl(firebaseStorage, context)

    }


}