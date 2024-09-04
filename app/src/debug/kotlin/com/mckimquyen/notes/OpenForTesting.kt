

package com.mckimquyen.notes

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class OpenClass

@OpenClass
@Target(AnnotationTarget.CLASS)
annotation class OpenForTesting
