local List = require("nvimdrive.linkedlist").List
local drive_channel = require("nvimdrive.drive_channel")

local M = {}

local buf_list = List:new()
local current_return = nil

M.new_buffer = function(fileid, filename)
    local buf = vim.api.nvim_create_buf(true, true)
    vim.api.nvim_set_option_value("filetype", "markdown", { buf = buf })
    vim.api.nvim_buf_set_name(buf, "drive:"  .. filename .. ".md")

    vim.keymap.set("n", DriveSettings.Mappings.DriveNext, function()
        M.next_buf()
        M.load_current_buf()
    end, {
        buffer = buf,
    })
    vim.keymap.set("n", DriveSettings.Mappings.DrivePreb, function()
        M.preb_buf()
        M.load_current_buf()
    end, {
        buffer = buf,
    })
    vim.keymap.set("n", DriveSettings.Mappings.DriveSave, function()
        drive_channel.upload_file_content(M.get_current().id, M.export_buffer_content())
        vim.notify("Cambios guardados en el Servicio") -- TODO: variable global
    end, {
        buffer = buf,
    })
    vim.keymap.set("n", DriveSettings.Mappings.DriveClose, function()
        M.close()
        M.load_current_buf()
    end, {
        buffer = buf,
    })

    buf_list:add({ buf = buf, id = fileid })
    M.load_current_buf()
    return buf
end

M.get_current = function()
    return buf_list:value()
end

M.get_buffer_content = function()
    return vim.api.nvim_buf_get_lines(buf_list:value().buf, 0, -1, true)
end

--- Metodo para cargar el contenido de un buffer en concreto. Internamente
--- se usa para cargar asincronamente el contenido.
M.set_buffer_content = function(buf, content)
    vim.api.nvim_buf_set_lines(buf, 0, -1, false, content)
end

M.export_buffer_content = function()
    local lines = vim.api.nvim_buf_get_lines(buf_list:value().buf, 0, -1, true)
    local content = table.concat(lines, "\n")
    return content
end

M.load_current_buf = function()
    if buf_list:value() then
        vim.api.nvim_set_current_buf(buf_list:value().buf)
    else
        vim.cmd("Ex")
    end
end

--- Muebe el current al siguiente elemento de la lista
--- es necesario llamar mas atarde a load_current_buf()
--- para renderizar el contenido en el bufer actual, o
--- a get_buffer_content() para optener el contenido en
--- forma de string[]
M.next_buf = function()
    buf_list:next()
end

M.preb_buf = function()
    buf_list:preb()
end

--- Método ad hoc para actualziar el id en una operación de creación
--- de ficheros sin bloquear el bucle de eventos.
M.set_current_id = function(fileid)
    buf_list:value().id = fileid
end

M.get_footer = function()
    local sb = {}
    local counter = 1
    local name
    local array = buf_list:to_array()
    for i = 1, #array do
        name = vim.fn.fnamemodify(vim.api.nvim_buf_get_name(array[i].value.buf), ":t:r")
        name = name:match("drive:(.+)")
        if #name > 10 then
            name = string.sub(name, 1, 8) .. "..."
        end
        sb[#sb + 1] = counter .. ":[" .. name .. (array[i].is_current and "]*" or "]")
        counter = counter + 1
    end

    return { table.concat(sb, " || ") }
end

--- Elimina el buffer cargado de la lista.
--- Es necesario llamar más tarde a load_current_buf.
M.close = function()
    vim.api.nvim_buf_delete(buf_list:value().buf, {})
    buf_list:delete_current()
end

--- Método auxiliar para la vista de navegación. Al acabar
--- las operaciones, llama a restore_current() para restaurar
--- el current guardado por este método.
M.save_current = function ()
    current_return = buf_list:value()
end


M.restore_current = function ()
    buf_list:set_current(current_return)
    current_return = nil
end

return M
