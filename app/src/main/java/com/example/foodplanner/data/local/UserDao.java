package com.example.foodplanner.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.foodplanner.data.model.User;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    Completable insertUser(User user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    Single<User> getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    Single<User> authenticate(String email, String passwordHash);

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    Single<Integer> checkEmailExists(String email);
}
