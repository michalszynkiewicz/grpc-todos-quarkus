package com.example;

import io.quarkus.test.junit.NativeImageTest;

@NativeImageTest
public class NativeTodoServiceIT extends TodoServiceTest {

    // Execute the same tests but in native mode.
}