/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#pragma version(1)
#pragma rs java_package_name(za.co.myconcepts.instaclone)
#pragma rs_fp_relaxed

// RELU activation function
float RS_KERNEL relu(float in) {
    if (in < 0.0f) {
        return 0.0f;
    } else {
        return in;
    }
}

// ELU activation function
float RS_KERNEL elu(float in) {
    float out = in;
    if (in < 0) {
         out = exp(in) - 1.0f;
    }
    return out;
}
