#!/bin/bash

# Cierra cualquier sesión llamada 'copernico_tests' si existe
tmux kill-session -t copernico_tests 2>/dev/null

# Inicia una nueva sesión de tmux llamada 'copernico_tests'
tmux new-session -d -s copernico_tests

# Panel 1: Ejecuta `docker start kafka cassandra` en el panel izquierdo abajo
#tmux send-keys -t copernico_tests:0.0 'docker stats' C-m
tmux split-window -h -t copernico_tests:0.0
tmux split-window -v -t copernico_tests:0.0
tmux split-window -v -t copernico_tests:0.0
#tmux split-window -v -t copernico_tests:0.0

tmux send-keys -t copernico_tests:0.0 'watch -n 0.1 "docker exec cassandra cqlsh -u cassandra -p cassandra -e \"select suj_identificador, suj_telefono, suj_email from read_side.buc_sujeto;\""' C-m
tmux send-keys -t copernico_tests:0.1 'watch -n 0.2 "docker exec cassandra cqlsh -u cassandra -p cassandra -e \"select soj_suj_identificador, soj_identificador, soj_descripcion from read_side.buc_sujeto_objeto;\""' C-m
tmux send-keys -t copernico_tests:0.2 'watch -n 0.3 "docker exec cassandra cqlsh -u cassandra -p cassandra -e \"select bob_soj_identificador, bob_obn_id, bob_estado, bob_saldo, bob_vencimiento from read_side.buc_obligaciones;\""' C-m

tmux send-keys -t copernico_tests:0.3 'docker start kafka cassandra' C-m
sleep 1
tmux send-keys -t copernico_tests:0.3 'docker exec -it cassandra cqlsh' C-m

tmux select-pane -t copernico_tests:0.3

# Muestra la sesión de tmux
tmux attach-session -t copernico_tests

#TODO: close the session and stop de dockers by entering some key

# Asigna la tecla 'q' para detener los contenedores y salir de tmux
#tmux send-keys -t copernico_tests:0.2 "echo 'Press q to stop Docker containers and exit tmux'" C-m
#tmux send-keys -t copernico_tests:0.2 "while read -rsn1 input; do if [[ \$input = q ]]; then docker stop kafka cassandra; tmux kill-session -t copernico_tests; break; fi; done" C-m
