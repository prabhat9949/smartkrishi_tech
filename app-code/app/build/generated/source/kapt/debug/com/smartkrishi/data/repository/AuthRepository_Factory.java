package com.smartkrishi.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class AuthRepository_Factory implements Factory<AuthRepository> {
  private final Provider<FirebaseAuth> firebaseAuthProvider;

  private final Provider<FirebaseDatabase> databaseProvider;

  public AuthRepository_Factory(Provider<FirebaseAuth> firebaseAuthProvider,
      Provider<FirebaseDatabase> databaseProvider) {
    this.firebaseAuthProvider = firebaseAuthProvider;
    this.databaseProvider = databaseProvider;
  }

  @Override
  public AuthRepository get() {
    return newInstance(firebaseAuthProvider.get(), databaseProvider.get());
  }

  public static AuthRepository_Factory create(Provider<FirebaseAuth> firebaseAuthProvider,
      Provider<FirebaseDatabase> databaseProvider) {
    return new AuthRepository_Factory(firebaseAuthProvider, databaseProvider);
  }

  public static AuthRepository newInstance(FirebaseAuth firebaseAuth, FirebaseDatabase database) {
    return new AuthRepository(firebaseAuth, database);
  }
}
