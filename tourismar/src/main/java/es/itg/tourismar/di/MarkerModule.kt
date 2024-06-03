package es.itg.tourismar.di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.itg.tourismar.data.repository.markerRepository.MarkerRepository
import es.itg.tourismar.data.repository.markerRepository.MarkerRepositoryImpl
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object MarkerModule {

    @Provides
    @Named("MarkerFirebaseFirestore")
    fun providesFirebaseFirestore() = FirebaseFirestore.getInstance()

    @Provides
    fun providesAnchorRepositoryImpl(@Named("MarkerFirebaseFirestore") firebaseFirestore: FirebaseFirestore): MarkerRepository {
        return MarkerRepositoryImpl(firebaseFirestore)
    }

}