package es.itg.tourismar.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.itg.tourismar.data.repository.authRepository.UserAuthRepository
import es.itg.tourismar.data.repository.authRepository.UserAuthRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun providesRepositoryImpl(firebaseAuth: FirebaseAuth):UserAuthRepository{
        return UserAuthRepositoryImpl(firebaseAuth)
    }

}