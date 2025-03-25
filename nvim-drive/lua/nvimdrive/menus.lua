local pickers = require("telescope.pickers")
local finders = require("telescope.finders")
local config = require("telescope.config").values
local previewers = require("telescope.previewers")
local actions = require("telescope.actions")
local action_state = require("telescope.actions.state")
local buffers = require("nvimdrive.buffers")
local drive_channel = require("nvimdrive.drive_channel")
local utils = require("nvimdrive.utils")

local M = {}

local win_opts = {
	events = {
		relative = "editor",
		width = 45,
		height = 5,
		col = vim.o.columns - 1,
		row = vim.o.lines - 1,
		style = "minimal",
		border = "rounded",
	},
	browser = {
		relative = "editor",
		width = 100,
		height = 30,
		style = "minimal",
		border = "rounded",
        title = DriveSettings.DriveService,
        title_pos = "center"
	},
	browser_footer = {
		relative = "editor",
		height = 1,
		style = "minimal",
		border = "rounded",
	},
}
win_opts.browser.col = math.floor((vim.o.columns - win_opts.browser.width) / 2)
win_opts.browser.row = math.floor((vim.o.lines - win_opts.browser.height) / 2)
win_opts.browser_footer.width = win_opts.browser.width
win_opts.browser_footer.col = win_opts.browser.col
win_opts.browser_footer.row = win_opts.browser.row + win_opts.browser.height + win_opts.browser_footer.height + 1

vim.api.nvim_set_hl(0, "TrueNormalFloat", { fg = "green", bg = "none", bold = true })
vim.api.nvim_set_hl(0, "TrueFloatBorder", { fg = "green", bg = "none", bold = true })
vim.api.nvim_set_hl(0, "TrueFloatTitle", { fg = "green", bg = "none", bold = true })
vim.api.nvim_set_hl(0, "FalseFloatBorder", { fg = "red", bg = "none", bold = true })
vim.api.nvim_set_hl(0, "FalseFloatTitle", { fg = "red", bg = "none", bold = true })
vim.api.nvim_set_hl(0, "FalseNormalFloat", { fg = "red", bg = "none", bold = true })

M.show_event = function(etype, title, lines, temporal)
	local float = -1
    local enter = false

    if etype == vim.NIL then
        enter = true
    end

	if title ~= nil then
		win_opts.events.title = title
		win_opts.events.title_pos = "center"
	end

	local saveheight = win_opts.events.height
	lines = lines or ""
	lines = utils.format_lines(win_opts.events.width, lines)
	if #lines < win_opts.events.height then
		win_opts.events.height = #lines
	end

	local buf = vim.api.nvim_create_buf(false, true)
	vim.api.nvim_set_option_value("bufhidden", "wipe", { buf = buf })
	vim.api.nvim_buf_set_lines(buf, 0, -1, true, lines)


	float = vim.api.nvim_open_win(buf, enter, win_opts.events)

	if etype == "good" then
		vim.api.nvim_set_option_value(
			"winhl",
			"FloatBorder:TrueFloatBorder,FloatTitle:TrueFloatTitle,NormalFloat:TrueNormalFloat",
			{ win = float }
		)
	elseif etype == "bad" then
		vim.api.nvim_set_option_value(
			"winhl",
			"FloatBorder:FalseFloatBorder,FloatTitle:FalseFloatTitle,NormalFloat:FalseNormalFloat",
			{ win = float }
		)
	end

	win_opts.events.height = saveheight

	if temporal then
		vim.defer_fn(function()
			vim.api.nvim_win_close(float, true)
		end, 2000)
	end

	return float
end

M.show_navegator = function()
	local buf_browser = vim.api.nvim_create_buf(false, true)
	local buf_footer = vim.api.nvim_create_buf(false, true)
	local content = buffers.get_buffer_content()
    buffers.save_current()

	vim.api.nvim_buf_set_lines(buf_footer, 0, -1, true, buffers.get_footer())
	vim.api.nvim_buf_set_lines(buf_browser, 0, -1, true, content)

	local win_browser = vim.api.nvim_open_win(buf_browser, true, win_opts.browser)
	local win_footer = vim.api.nvim_open_win(buf_footer, true, win_opts.browser_footer)

	vim.keymap.set("n", DriveSettings.Mappings.DriveNext, function()
		buffers.next_buf()
        vim.api.nvim_buf_set_lines(buf_footer, 0, -1, true, buffers.get_footer())
        vim.api.nvim_buf_set_lines(buf_browser, 0, -1, true, buffers.get_buffer_content())
	end, {
		buffer = buf_footer,
	})
	vim.keymap.set("n", DriveSettings.Mappings.DrivePreb, function()
		buffers.preb_buf()
        vim.api.nvim_buf_set_lines(buf_footer, 0, -1, true, buffers.get_footer())
        vim.api.nvim_buf_set_lines(buf_browser, 0, -1, true, buffers.get_buffer_content())
	end, {
		buffer = buf_footer,
	})
	vim.keymap.set("n", "<CR>", function()
        buffers.save_current() -- pequeño truco para saltarse el restores_current al cerrar la ventana
        vim.api.nvim_win_close(win_footer, true)
        buffers.load_current_buf()
	end, {
		buffer = buf_footer,
	})
	vim.keymap.set("n", DriveSettings.Mappings.DriveClose, function()
		buffers.close()
        vim.api.nvim_buf_set_lines(buf_footer, 0, -1, true, buffers.get_footer())
        vim.api.nvim_buf_set_lines(buf_browser, 0, -1, true, buffers.get_buffer_content())
	end, {
		buffer = buf_footer,
	})

	vim.api.nvim_create_autocmd("WinClosed", {
		pattern = tostring(win_footer),
		callback = function()
            buffers.restore_current()
			vim.api.nvim_win_close(win_browser, true)
            vim.api.nvim_buf_delete(buf_browser, {force = true})
            vim.api.nvim_buf_delete(buf_footer, {force = true})
		end,
	})
end

M.show_finder = function(files, opts)
	pickers
		.new(opts, {
			finder = finders.new_table({
				results = files,
				entry_maker = function(entry)
					return {
						value = entry,
						display = entry.name,
						ordinal = entry.name,
					}
				end,
			}),
			sorter = config.generic_sorter(opts),
			previewer = previewers.new_buffer_previewer({
				title = "Información del archivo",
				define_preview = function(self, entry)
					vim.api.nvim_buf_set_lines(self.state.bufnr, 0, 0, true, vim.split(vim.inspect(entry.value), "\n"))
				end,
			}),
			attach_mappings = function(prompt_bufnr)
				actions.select_default:replace(function()
					local selection = action_state.get_selected_entry().value.id
					actions.close(prompt_bufnr)
                    local new_buffer = buffers.new_buffer(
                        selection,
                        action_state.get_selected_entry().value.name
                    )

                    local content = drive_channel.get_file_content(selection)
                    if content then
                        content = utils.format_lines(80, vim.split(content, "\n"))
                        buffers.set_buffer_content(new_buffer, content)
                    end
				end)

				return true
			end,
		})
		:find()
end

return M
