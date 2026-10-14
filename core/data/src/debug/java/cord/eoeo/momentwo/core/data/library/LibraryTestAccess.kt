package cord.eoeo.momentwo.core.data.library
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
@EntryPoint @InstallIn(SingletonComponent::class)
interface LibraryTestAccess {fun repository(): LocalLibraryRepository;fun media(): LocalMediaStore}
