#1- Alta VSO 1
kafkacat -b localhost:9092 -t DGR-COP-OBJETOS-TRI -P assets/examples/test/exclusiones/1-DGR-COP-OBJETOS-TRI-ONGOING.json

#2- Alva VSO 2
kafkacat -b localhost:9092 -t DGR-COP-OBJETOS-TRI -P assets/examples/test/exclusiones/2-DGR-COP-OBJETOS-TRI-ONGOING.json

#3- Alta VSO 3 (Responsable)
kafkacat -b localhost:9092 -t DGR-COP-OBJETOS-TRI -P assets/examples/test/exclusiones/3-DGR-COP-OBJETOS-TRI-ONGOING.json

#4- Alta Obn Vencida VSO 3
kafkacat -b localhost:9092 -t DGR-COP-OBLIGACIONES-TRI -P assets/examples/test/exclusiones/4-DGR-COP-OBLIGACIONES-TRI-ONGOING.json

#5- Alta ExclusionObjeto E VSO 2
kafkacat -b localhost:9092 -t DGR-COP-OBJETOS-TRI -P assets/examples/test/exclusiones/5-DGR-COP-OBJETOS-TRI-ONGOING.json

#6- Vencimiento ExclusionObjeto VSO 2
kafkacat -b localhost:9092 -t DGR-COP-OBJETOS-TRI -P assets/examples/test/exclusiones/6-DGR-COP-OBJETOS-TRI-ONGOING.json

#7- Alta ExclusionObjeto NE VSO 1
kafkacat -b localhost:9092 -t DGR-COP-OBJETOS-TRI -P assets/examples/test/exclusiones/7-DGR-COP-OBJETOS-TRI-ONGOING.json

#8- Pago de Obn Vencida VSO 3
kafkacat -b localhost:9092 -t DGR-COP-OBLIGACIONES-TRI -P assets/examples/test/exclusiones/8-DGR-COP-OBLIGACIONES-TRI-ONGOING.json

#9- Vencimiento ExclusionObjeto NE VSO 1
kafkacat -b localhost:9092 -t DGR-COP-OBJETOS-TRI -P assets/examples/test/exclusiones/9-DGR-COP-OBJETOS-TRI-ONGOING.json

#10- Alta Exclusion Sujeto NE
kafkacat -b localhost:9092 -t DGR-COP-SUJETO-TRI -P assets/examples/test/exclusiones/10-DGR-COP-SUJETO-TRI-ONGOING.json