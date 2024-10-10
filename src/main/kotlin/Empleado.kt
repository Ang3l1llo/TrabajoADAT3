package org.example

class Empleado (val id : Int, val apellido : String, val departamento : String, val salario : Double){

    override fun toString(): String {
        return "Empleado(id=$id, apellido='$apellido', departamento='$departamento', salario=$salario)"
    }
}