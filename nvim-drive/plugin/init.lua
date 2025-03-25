vim.api.nvim_create_user_command("DriveInit", function()
    require("nvimdrive.drive").init()
end, {})
