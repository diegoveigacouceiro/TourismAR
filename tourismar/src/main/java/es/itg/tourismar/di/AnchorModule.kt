package es.itg.tourismar.di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.itg.tourismar.data.repository.anchorRepository.AnchorRepository
import es.itg.tourismar.data.repository.anchorRepository.AnchorRepositoryImpl
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AnchorModule {

    @Provides
    @Named("AnchorFirebaseFirestore")
    fun providesFirebaseFirestore() = FirebaseFirestore.getInstance()

    @Provides
    fun providesAnchorRepositoryImpl(@Named("AnchorFirebaseFirestore") firebaseFirestore: FirebaseFirestore): AnchorRepository {
        return AnchorRepositoryImpl(firebaseFirestore)
    }
}