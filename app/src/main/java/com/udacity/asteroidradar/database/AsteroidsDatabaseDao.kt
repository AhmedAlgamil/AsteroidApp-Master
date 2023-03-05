/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.udacity.asteroidradar.Asteroid

@Dao
interface AsteroidsDatabaseDao{

    @Insert
    suspend fun insert(asteroid : Asteroid)

    @Update
    fun update(asteroid : Asteroid)

    @Query("SELECT * FROM asteroids_table_approach WHERE id = :key")
    fun get(key : Long) : Asteroid

    @Query("DELETE FROM asteroids_table_approach")
    suspend fun clear()

    @Query("SELECT * FROM asteroids_table_approach WHERE close_approach_date BETWEEN :startDate AND :endDate ORDER BY id DESC")
    fun getAllAsteroids(startDate:String,endDate :String) : LiveData<List<Asteroid>>

    @Query("SELECT * FROM asteroids_table_approach ORDER BY id DESC LIMIT 1")
    fun getAsteroid() : Asteroid?

}
