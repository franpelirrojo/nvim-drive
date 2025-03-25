#!/bin/bash

session_name=$(basename "$PWD")

tmux has-session -t "$session_name" 2>/dev/null

if [ $? != 0 ]; then
  tmux new-session -d -s "$session_name" -n backend
  tmux send-keys -t "$session_name:backend" 'cd ./nvim-drive-server' C-m
  tmux send-keys -t "$session_name:backend" 'nvim .' C-m

  tmux new-window -t "$session_name" -n plugin 
  tmux send-keys -t "$session_name:plugin" 'cd ./nvim-drive' C-m
  tmux send-keys -t "$session_name:plugin" 'nvim .' C-m

  tmux new-window -t "$session_name" -n terminal

  tmux new-window -t "$session_name" -n doc
  tmux send-keys -t "$session_name:doc" 'cd ./doc' C-m
  tmux send-keys -t "$session_name:doc" 'nvim .' C-m

  tmux select-window -t "$session_name:backend"
fi

tmux attach-session -t "$session_name"
