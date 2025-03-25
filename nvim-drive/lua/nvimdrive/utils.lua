local M = {}

M.format_lines = function(textwidth, lines)
    if not textwidth then
        textwidth = DriveSettings.DriveFileLength
    end

	local formatted = {}
	local resto = ""
	for _, line in ipairs(lines) do
	    if line:match("^%s*$") then
			if #resto > 0 then
				table.insert(formatted, resto)
				resto = ""
			end
			table.insert(formatted, "")
		else
			if #resto > 0 then
				line = resto .. line
				resto = ""
			end

			while #line > textwidth do
				local limite = textwidth
				while limite > 1 and string.sub(line, limite, limite) ~= " " do
					limite = limite - 1
				end
				if limite > 1 then
					table.insert(formatted, string.sub(line, 1, limite - 1))
					line = string.sub(line, limite + 1)
				else
					table.insert(formatted, string.sub(line, 1, textwidth))
					line = string.sub(line, textwidth + 1)
				end
			end
			if #line > 0 then
				table.insert(formatted, line)
			end
		end
	end
	return formatted
end

return M
