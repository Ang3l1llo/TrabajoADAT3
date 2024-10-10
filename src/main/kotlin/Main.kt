package org.example

import org.w3c.dom.*
import java.io.BufferedReader
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.*
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun main() {
    val rutaFichero = Path.of("src/main/resources/empleados.csv")
    val rutaFicheroXML = Path.of("src/main/resources/empleadosXML.xml")
    //Punto1
    val listaEmpleados = leerFichero(rutaFichero)

    //Punto2
    generarXml(listaEmpleados,rutaFicheroXML)

    //Punto3
    modificarNodo(7,2000.0,rutaFicheroXML)

    //Punto4
    val listaEmpleadosModificados = leerXmlModificado(rutaFicheroXML)

    for(empleado in listaEmpleadosModificados){
        println(empleado)
    }

}

fun leerFichero(fichero: Path): List<Empleado> {
    //Primero se inicializa la lista donde vamos a guardar los empleados
    val listaEmpleados: MutableList<Empleado> = mutableListOf()

    val br: BufferedReader = Files.newBufferedReader(fichero)

    //Se va iterando mediante el buffer
    br.use { reader ->
        reader.readLine()  //Se lee la primera línea para ignorarla porq es la cabecera
        var linea = reader.readLine()  //Ahora si en la variable línea leemos la primera línea de datos
        while (linea != null) {
            val campos = linea.split(",")  // Para almacenar cada dato separado
            val empleado = Empleado(  // Se crea un objeto empleado con los valores de los campos que acabamos de separar
                id = campos[0].toInt(),
                apellido = campos[1],
                departamento = campos[2],
                salario = campos[3].toDouble()
            )
            listaEmpleados.add(empleado)
            linea = reader.readLine()  // Lee la siguiente línea hasta que llegue al final y termine en null
        }
    }
    return listaEmpleados
}

fun generarXml(lista : List<Empleado>, fichero : Path){
    //Primero se declara lo necesario para poder trabajar
    val dbf : DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
    val db : DocumentBuilder = dbf.newDocumentBuilder()
    val imp : DOMImplementation = db.domImplementation
    val document : Document = imp.createDocument(null,"empleados",null)

    for(empleado in lista){
        //Creamos el element y lo enlazamos al nodo padre
        val nodoEmpleado : Element = document.createElement("empleado")
        document.documentElement.appendChild(nodoEmpleado)
        //Esto permite establecer el atributo id y se agrega al nodo/etiqueta empleado. Se pasa a string para poder representarlo en el xml
        nodoEmpleado.setAttribute("id", empleado.id.toString())

        //Ahora los hijos
        val apellido : Element = document.createElement("apellido")
        val departamento : Element = document.createElement("departamento")
        val salario : Element = document.createElement("salario")

        //Aquí en lugar de escribirlo manualmente le pasamos los atributos de la clase
        val textoApellido : Text = document.createTextNode(empleado.apellido)
        val textoDepartamento : Text = document.createTextNode(empleado.departamento)
        val textoSalario : Text = document.createTextNode(empleado.salario.toString())

        //Ahora unimos el textnode al elemento correspondiente
        apellido.appendChild(textoApellido)
        departamento.appendChild(textoDepartamento)
        salario.appendChild(textoSalario)

        //Unimos al nodo padre
        nodoEmpleado.appendChild(apellido)
        nodoEmpleado.appendChild(departamento)
        nodoEmpleado.appendChild(salario)
    }
    //Ahora creamos el XML
    val source : Source = DOMSource(document)
    val result : Result = StreamResult(fichero.toFile())
    val transformer : Transformer = TransformerFactory.newInstance().newTransformer()

    transformer.setOutputProperty(OutputKeys.INDENT,"yes")
    transformer.transform(source,result)
}

fun modificarNodo(idEmpleado : Int, salarioModificado: Double, fichero: Path){
    //Primero se declara lo necesario para poder trabajar con el fichero
    val dbf : DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
    val db : DocumentBuilder = dbf.newDocumentBuilder()
    val document : Document = db.parse(fichero.toFile())
    val root : Element = document.documentElement
    root.normalize()

    val listaNodos : NodeList = root.getElementsByTagName("empleado")

    for(i in 0..<listaNodos.length){
        val nodoEmpleado = listaNodos.item(i) as Element
        val iD = nodoEmpleado.getAttribute("id").toInt() //Lo pasamos a int para poder compararlo con el id que le pasamos

        if(iD == idEmpleado){
            val elementoSalario = nodoEmpleado.getElementsByTagName("salario").item(0) as Element
            elementoSalario.textContent = salarioModificado.toString()

            break
        }

    }
    //Ahora crear el XML
    val source : Source = DOMSource(document)
    val result : Result = StreamResult(fichero.toFile())
    val transformer : Transformer = TransformerFactory.newInstance().newTransformer()

    transformer.setOutputProperty(OutputKeys.INDENT, "no") //Para evitar el doble salto de línea
    transformer.transform(source,result)
}

fun leerXmlModificado(fichero : Path) : List<Empleado> {
    val empleados : MutableList<Empleado> = mutableListOf()

    val dbf : DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
    val db : DocumentBuilder = dbf.newDocumentBuilder()
    val document : Document = db.parse(fichero.toFile())
    val root : Element = document.documentElement
    root.normalize()
    val listaNodos : NodeList = root.getElementsByTagName("empleado")

    for(i in 0..<listaNodos.length){
        val nodo : Node = listaNodos.item(i)

        if(nodo.nodeType == Node.ELEMENT_NODE){
            val elementoNodo : Element = nodo as Element

            val elementoId = elementoNodo.getAttribute("id")
            val elementoApellido : NodeList = elementoNodo.getElementsByTagName("apellido")
            val elementoDepartamento : NodeList = elementoNodo.getElementsByTagName("departamento")
            val elementoSalario : NodeList = elementoNodo.getElementsByTagName("salario")

            val empleado = Empleado(
                id = elementoId.toInt(),
                apellido = elementoApellido.item(0).textContent,
                departamento = elementoDepartamento.item(0).textContent,
                salario = elementoSalario.item(0).textContent.toDouble()
            )
            empleados.add(empleado)
        }
    }
    return empleados
}



