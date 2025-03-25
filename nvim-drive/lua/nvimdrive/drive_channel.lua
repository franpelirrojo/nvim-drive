local M = {}

local java_socket_id = nil
local java_job_id = nil

local check_channel = function()
	if not java_socket_id then
        vim.notify("No se pudo conectar al servicio de " .. DriveSettings.DriveService .. ". Asegúrate de que el plugin está activo.",
            vim.log.levels.ERROR, {})
        return false
	end

    return true
end

M.launch_drive = function(command)
	local source_file = debug.getinfo(1, "S").source:sub(2)
	local jar_path = vim.fn.fnamemodify(source_file, ":h")
	local append_data = function(_, data)
		if #data > 0 then
			P(data) -- TODO: notificación
		end
	end

	java_job_id = vim.fn.jobstart(command, {
		cwd = jar_path,
		stdout_buffered = true,
		on_stderr = append_data,
	})

	-- El channel del socket del servidor RPC siempre
	-- se crea despeus de lanzar el jar, por eso el +1.
	-- El canal del job es el sterr.
	java_socket_id = java_job_id + 1
end

M.open_files = function()
	if not check_channel() then
		return nil
	end

	return vim.fn.rpcrequest(java_socket_id, "get_files", {})
end

M.get_file_content = function(fileid)
	if not check_channel() then
		return nil
	end

    return vim.fn.rpcrequest(java_socket_id, "file_content", fileid)
end

M.upload_file_content = function(fileid, content)
	if not check_channel() then
		return nil
	end
	vim.fn.rpcnotify(java_socket_id, "update_content", { fileid, content })
end

M.create_file = function(filename, content)
	if not check_channel() then
		return nil
	end
	return vim.fn.rpcnotify(java_socket_id, "create_file", { filename, content })
end

M.delete_file = function(fileid)
	if not check_channel() then
		return nil
	end

	vim.fn.rpcnotify(java_socket_id, "delete_file", { fileid })
    return 0
end

M.stop_process = function()
	if not java_job_id then
		return
	end
	vim.fn.jobstop(java_job_id)
    java_job_id = nil
    java_socket_id = nil
end

return M
