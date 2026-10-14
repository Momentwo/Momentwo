package cord.eoeo.momentwo.core.data.library
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
@EntryPoint @InstallIn(SingletonComponent::class)
interface LibraryTestAccess {fun database(): cord.eoeo.momentwo.core.database.MomentwoDatabase;fun preferences(): cord.eoeo.momentwo.core.datastore.PreferenceRepository;fun repository(): LocalLibraryRepository;fun media(): LocalMediaStore}
