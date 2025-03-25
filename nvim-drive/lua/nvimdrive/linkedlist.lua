---@diagnostic disable: need-check-nil

local M = {}

--- Nodo de una lista enalzada
--- @class Node
--- @field next Node|nil
--- @field preb Node|nil
--- @field value any
local Node = {}

--- Node constructor
--- @param value any
--- @return Node
function Node:new(value)
	local o = { value = value, next = nil, preb = nil }
	setmetatable(o, self)
	self.__index = self
	return o
end

--- Compara si dos nodos son iguales
--- @param node2 Node
--- @return boolean
function Node:equals(node2)
	if self == node2 then
		return true
	end

	if self.value ~= node2.value then
		return false
	end

	if self.next ~= node2.next or self.preb ~= node2.preb then
		return false
	end

	return true
end

--- Lista circular. Estructura subyacente a los buffers de nvimdrive.
--- @class List
--- @field list Node|nil
--- @field size integer
--- @field current Node|nil
local List = {}

--- List constructor
--- @return List
function List:new()
	local o = { list = nil, size = 0, current = nil }
	setmetatable(o, self)
	self.__index = self
	return o
end

--- Añade un nuevo nodo. Ese nodo se vuelve el current
--- @param self List
--- @param value any
function List:add(value)
	local newnode = Node:new(value)
	if not self.list then
		self.list = newnode
		self.current = self.list
	else
		local head = self.list
		if not head.preb then
			head.next = newnode
			head.preb = newnode
			newnode.preb = head
			newnode.next = head
		else
			local tail = head.preb
			tail.next = newnode
			newnode.preb = tail
			newnode.next = head
			head.preb = newnode
		end
	end

    self.current = newnode
	self.size = self.size + 1
end

--- Hace que el nodo en la posición del indice sea el current 
--- @param self List
--- @param value any 
function List:set_current(value)
	if not value then
		error("value == nil")
	end

	local pointer = 1
	local node = self.list
	while pointer ~= self.size do
		pointer = pointer + 1
        if node.value.buf == value.buf
            and node.value.id == value.id then
            break
        end

		node = node.next
	end

    self.current = node
end

--- Elimina el current y se mueve al siguiente nodo.
function List:delete_current()
	local deletenode = self.current
    self.current = deletenode.next

	if self.size == 1  then
		self.list = nil
        self.current = nil
	elseif self.size == 2 then
		local nextnode = deletenode.next
        deletenode.next = nil
        deletenode.preb = nil
        nextnode.next = nil
        nextnode.preb = nil
        self.list = nextnode
    else
		local prenode = deletenode.preb
		local nextnode = deletenode.next
        deletenode.preb = nil
        deletenode.next = nil
		prenode.next = nextnode
		nextnode.preb = prenode

		if deletenode:equals(self.list) then
		    self.list = nextnode
		end
    end

	self.size = self.size - 1
end

function List:next()
	if self.current.next then
		self.current = self.current.next
		return self.current
	end
end

function List:preb()
	if self.current.preb then
		self.current = self.current.preb
		return self.current
	end
end

function List:value()
    if not self.current then
        return nil
    end

	return self.current.value
end

function List:to_array()
    local array = {}
    local node = self.list
    for i = 1, self.size do
        local is_current = node:equals(self.current) and true or false
        array[i] = {value = node.value, is_current = is_current}
		node = node.next
    end

    return array
end

M.Node = Node
M.List = List

return M
