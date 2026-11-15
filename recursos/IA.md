#Herramienta 1
· Chat GPT

#Prompts principales

1. Quiero hacer una tienda de componentes informaticos como tarjetas graficas, ratones, teclados, monitores,etc. Necesito que me hagas un esquema bien redactado con fotos. Se deben incluir clases y metodos relacionados con JDBC como viene indicado en el pdf adjunto.

2. En el esquema incluye la creacion de 6 pruebas de ejecucion y la configuracion del log4j

3. Donde veo la BBDD de H2 creada? Y como se si se ha creado la de mysql?

4. Quiero que separes las responsabilidades en la creacion inicial de la base de datos y la inserción de datos. Configura las properties del h2 para que se quede persistido en una carpeta dentro del programa

5. La base de datos debe inicializar en un estado inicial, no debe incluir las modificaciones o los registros añadidos en la simulaciónn

6. quiero estos cambios en el modelado de tablas de la base de datos:
1- Solo quiero 5 categorias para los productos
2- 13 productos que esten relacionados con su categoria correspondiente
3- Solo quiero 5 clientes registrados
4- 8 direcciones, algunos clientes deben tener mas de 1 dirección
5- Creame solo 5 pedidos que tengan varios productos, elimina los pedidos de un solo producto
6- En movimientos-stock se deben registrar las modificaciones de stock
7- Los pagos pueden quedar registrados como pendientes y los pedidos como pendientes o cancelados

Para hacer las siguiente operaciones en el main, además de usar DAO, se deben crear procedimientos en la BBDD para llamarlos desde la simulacion y guardar los objetos en DTOs
En la simulación, quiero que se deje un lapso de tiempo entre cada accion siga este procedimiento:
1- Crear tablas y añadir registros
2- Mostrar los clientes que tiene la tienda con todas sus direcciones
3- Mostrar los productos que tiene la tienda ordenados por categoria y mostrar la cantidad disponible de cada producto
4- Mostrar el historial de movimientos solo de aquellos productos que han sido modificados, hacer un join para mostrar con detalle cada producto y no mostrar los productos no modificados
5- Hacer 3 pedidos en los que se deberá mostrar con detalle el pedido completo, con sus productos, precios y cantidades y el pago de dicho pedido y mostrarlos en pantalla

7. Vale, quiero que hagas lo siguiente:
Para mostrar las consultas realizadas de la simulacion, se deben  crear DTOs y leer la información de ellas:
1- Se debe crear la clase CategoriaDTO (con id y nombre) que contenga una lista de productoDTO( que contiene nombre, precio y stock)
2- Se debe crear la clase MoviemientoDTO con id, nombre del producto,el tipo de movimiento(compra,venta o ajuste) y fecha en formato ( dia:mes:año   hora:minutos:segundos
3- Crear la clase ClienteDTO con id, nombre, correo y una lista de direcciones
4- Crear PedidoDTO con id, nombre cliente, email cliente, direccion1 del cliente,un mapa con los distintos productos y su cantidad, el precio total y el pago. En la consola se debe mostrar todo menos el id, y se debe mostrar tambien el precio por unidad del producto y la cantidad que tiene
5- Crear un ProductoDTO con id,nombre y precio.

Se debe mostrar en consola de forma ordenada ej:
Usuarios:
1- Ana....
2- Alvaro...


8. Crea 6 pruebas junit que demuestren el funcionamiento del proyecto, sin crear ficheros log ni modificaciones en la apliucacion o bbdd

9. Reescribe el codigo de forma que este bien estructurado y se pueda entender el proceso paso a paso, deben estar todos los pasos bien seperados como en la foto que se adjunta 

10. Analioza el proyecto y creame un diagrama de entidad relacion 

11. Quiero que crees el diagrama en un fichero dentro del proyecto 

12. Debe ser relacional, con relaciones 1:N, 1:1,N:M,solo eso, no se necesita saber que s ehace en la aplicaicon
