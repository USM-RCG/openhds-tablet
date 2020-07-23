package org.cimsbioko.navconfig.forms

interface Binding {
    val name: String
    val form: String
    val label: String
    val builder: FormBuilder
    val consumer: FormConsumer
}