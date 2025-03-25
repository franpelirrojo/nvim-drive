DriveSettings = {
	DriveFileLength = 80,
	DriveService = "Google Drive",
	Mappings = {
		DriveFind = "<leader>Df",
		DriveOpen = "<leader>Do",
		DriveSave = "<leader>w",
		DriveNext = "n",
		DrivePreb = "N",
		DriveClose = "<leader>q",
		DriveBrowse = "<leader>Db",
	},
}

local drive_channel = require("nvimdrive.drive_channel")
local buffers = require("nvimdrive.buffers")
local menus = require("nvimdrive.menus")

local M = {}


M.setup = function(settings)
	DriveSettings = vim.tbl_extend("force", DriveSettings, settings or {})
end

local single = false
M.init = function()
	if single then
		return
	else
		single = true
	end

    -- TODO: hacer esto relativo
    drive_channel.launch_drive({ "java", "-jar", "../../nvimdrive-v.0.8.0.jar", vim.v.servername })

	vim.api.nvim_create_user_command("DriveFind", function()
        local files = drive_channel.open_files()
        if files then
		    menus.show_finder(files, _)
        end
	end, {})

	vim.api.nvim_create_user_command("DriveBrowse", function()
		menus.show_navegator()
	end, {})

	vim.api.nvim_create_user_command("DriveStop", function()
		drive_channel.stop_process()
	end, {})

	vim.api.nvim_create_user_command("DriveOpen", function()
		buffers.load_current_buf()
	end, {})

	vim.api.nvim_create_user_command("DriveCreate", function()
		local filename = vim.fn.fnamemodify(vim.api.nvim_buf_get_name(0), ":t:r")
        local filecontent = vim.api.nvim_buf_get_lines(0, 0, -1, true)
        vim.api.nvim_buf_delete(0, { force = true })
        local newbuf = buffers.new_buffer(_, filename)
        buffers.set_buffer_content(newbuf, filecontent)
        drive_channel.create_file(filename, table.concat(filecontent, "\n"))
	end, {})

	vim.api.nvim_create_user_command("DriveDelete", function()
        local response = drive_channel.delete_file(buffers.get_current().id)
        if response then
            buffers.close()
            buffers.load_current_buf()
        end
	end, {})

	vim.keymap.set("n", DriveSettings.Mappings.DriveFind, "<cmd>DriveFind<CR>", { desc = "Abre un buscador" })
	vim.keymap.set("n", DriveSettings.Mappings.DriveOpen, "<cmd>DriveOpen<CR>", { desc = "Abre el último buffer de drive" })
	vim.keymap.set( "n", DriveSettings.Mappings.DriveBrowse, "<cmd>DriveBrowse<CR>", { desc = "Abre un menu de navegación de los buffers cargados" })
end

return M
